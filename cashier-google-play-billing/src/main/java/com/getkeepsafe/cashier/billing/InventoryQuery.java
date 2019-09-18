package com.getkeepsafe.cashier.billing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.getkeepsafe.cashier.Inventory;
import com.getkeepsafe.cashier.InventoryListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.Vendor;
import com.getkeepsafe.cashier.VendorConstants;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Inventory query helper class. Performs api calls to get requested products info and purchases.
 * Since cashier purchase contains full product info, but Google Billing only returns order id and
 * receipt, getSkuDetails call must be performed for all purchased skus.
 */
class InventoryQuery {

    private Threading threading;

    private InventoryListener listener;

    private AbstractGooglePlayBillingApi api;

    /**
     * Inapp product details returned from async getSkuDetails call
     * Non null value indicates that sku details query finished
     */
    private List<SkuDetails> inappSkuDetails = null;

    /**
     * Subscription product details returned from async getSkuDetails call
     * Non null value indicates that sku details query finished
     * */
    private List<SkuDetails> subsSkuDetails = null;

    /**
     * List of purchases of both inapp and subscription types
     * Non null value indicates that purchases query finished
     */
    private List<com.android.billingclient.api.Purchase> purchases = null;

    private Collection<String> inappSkus;

    private Collection<String> subSkus;

    private int inappResponseCode = 0;

    private int subsResponseCode = 0;

    private boolean notified = false;

    /**
     * Query inventory.
     * @param api Google Play Billing API instance.
     * @param listener Listener to deliver success / error.
     * @param inappSkus List of product skus of item type to query. May be null.
     * @param subSkus List of product skus of subscription type to query. May be null.
     */
    static void execute(@NonNull AbstractGooglePlayBillingApi api, @NonNull InventoryListener listener, @Nullable Collection<String> inappSkus, @Nullable Collection<String> subSkus) {
        execute(new Threading(), api, listener, inappSkus, subSkus);
    }

    static void execute(@NonNull Threading threading, @NonNull AbstractGooglePlayBillingApi api, @NonNull InventoryListener listener, @Nullable Collection<String> inappSkus, @Nullable Collection<String> subSkus) {
        new InventoryQuery(threading, api, listener, inappSkus, subSkus).execute();
    }

    private InventoryQuery(@NonNull Threading threading, @NonNull AbstractGooglePlayBillingApi api, @NonNull InventoryListener listener, @Nullable Collection<String> inappSkus, @Nullable Collection<String> subSkus) {
        this.threading = threading;
        this.api = api;
        this.listener = listener;
        this.inappSkus = inappSkus;
        this.subSkus = subSkus;
    }

    private void execute() {
        // Execute on new thread to avoid blocking UI thread
        threading.runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!api.available()) {
                        listener.failure(new Vendor.Error(VendorConstants.INVENTORY_QUERY_UNAVAILABLE, -1));
                        return;
                    }

                    inappSkuDetails = null;
                    subsSkuDetails = null;
                    Set<String> inappSkusToQuery = new HashSet<>();
                    Set<String> subSkusToQuery = new HashSet<>();
                    boolean subscriptionsSupported = api.isBillingSupported(BillingClient.SkuType.SUBS) == BillingClient.BillingResponse.OK;

                    if (inappSkus != null) {
                        inappSkusToQuery.addAll(inappSkus);
                    }
                    if (subSkus != null) {
                        subSkusToQuery.addAll(subSkus);
                    }

                    // Get purchases of both types
                    List<com.android.billingclient.api.Purchase> inappPurchases = api.getPurchases(BillingClient.SkuType.INAPP);
                    List<com.android.billingclient.api.Purchase> subPurchases = subscriptionsSupported ? api.getPurchases(BillingClient.SkuType.SUBS)
                            : new ArrayList<com.android.billingclient.api.Purchase>();

                    if (inappPurchases == null || subPurchases == null) {
                        // If any of two getPurchases call didn't return result, return error
                        listener.failure(new Vendor.Error(VendorConstants.INVENTORY_QUERY_FAILURE, -1));
                        return;
                    }

                    purchases = new ArrayList<>();
                    purchases.addAll(inappPurchases);
                    purchases.addAll(subPurchases);

                    // Add all inapp purchases skus to skus to be queried list
                    for (com.android.billingclient.api.Purchase inappPurchase : inappPurchases) {
                        inappSkusToQuery.add(inappPurchase.getSku());
                    }
                    // Add all subscription purchases skus to skus to be queried list
                    for (com.android.billingclient.api.Purchase subPurchase : subPurchases) {
                        subSkusToQuery.add(subPurchase.getSku());
                    }

                    if (inappSkusToQuery.size() > 0) {
                        // Perform async sku details query
                        api.getSkuDetails(BillingClient.SkuType.INAPP, new ArrayList<String>(inappSkusToQuery), new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                                inappSkuDetails = skuDetailsList != null ? skuDetailsList : new ArrayList<SkuDetails>();
                                inappResponseCode = responseCode;
                                // Check if other async operation finished
                                notifyIfReady();
                            }
                        });
                    } else {
                        inappSkuDetails = Collections.emptyList();
                    }

                    if (subSkusToQuery.size() > 0 && subscriptionsSupported) {
                        // Perform async sku details query
                        api.getSkuDetails(BillingClient.SkuType.SUBS, new ArrayList<String>(subSkusToQuery), new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                                subsSkuDetails = skuDetailsList != null ? skuDetailsList : new ArrayList<SkuDetails>();
                                subsResponseCode = responseCode;
                                // Check if other async operation finished
                                notifyIfReady();
                            }
                        });
                    } else {
                        subsSkuDetails = Collections.emptyList();
                    }

                    // Check if result may be delivered.
                    // Covers case with empty skus and purchase lists
                    notifyIfReady();

                } catch (Exception e) {
                    listener.failure(new Vendor.Error(VendorConstants.INVENTORY_QUERY_UNAVAILABLE, -1));
                }
            }
        });
    }

    private synchronized void notifyIfReady() {
        // When all three variables are not null, all async operations are finished
        // and result may be delivered to listener
        if (purchases != null && inappSkuDetails != null && subsSkuDetails != null && !notified) {

            if (inappResponseCode != BillingClient.BillingResponse.OK || subsResponseCode != BillingClient.BillingResponse.OK) {
                // Deliver result on main thread
                threading.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.failure(new Vendor.Error(VendorConstants.INVENTORY_QUERY_FAILURE, Math.max(inappResponseCode, subsResponseCode)));
                    }
                });
                notified = true;
                return;
            }

            final Inventory inventory = new Inventory();

            // Map of sku -> details
            Map<String, SkuDetails> details = new HashMap<>();

            for (SkuDetails itemDetails : inappSkuDetails) {
                details.put(itemDetails.getSku(), itemDetails);
                if (inappSkus != null && inappSkus.contains(itemDetails.getSku())) {
                    // Return product details only when requested in inappSkus param
                    inventory.addProduct(GooglePlayBillingProduct.create(itemDetails, BillingClient.SkuType.INAPP));
                }
            }

            for (SkuDetails subDetail : subsSkuDetails) {
                details.put(subDetail.getSku(), subDetail);
                if (subSkus != null && subSkus.contains(subDetail.getSku())) {
                    // Return product details only when requested in subSkus param
                    inventory.addProduct(GooglePlayBillingProduct.create(subDetail, BillingClient.SkuType.SUBS));
                }
            }

            for (com.android.billingclient.api.Purchase billingPurchase : purchases) {
                SkuDetails skuDetails = details.get(billingPurchase.getSku());
                if (skuDetails != null) {
                    Product product = GooglePlayBillingProduct.create(skuDetails, skuDetails.getType());
                    try {
                        Purchase purchase = GooglePlayBillingPurchase.create(product, billingPurchase);
                        inventory.addPurchase(purchase);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Deliver result on main thread
                        threading.runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.failure(new Vendor.Error(VendorConstants.INVENTORY_QUERY_MALFORMED_RESPONSE, -1));
                            }
                        });
                        return;
                    }
                }
            }

            // Deliver result on main thread
            threading.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    listener.success(inventory);
                }
            });
            notified = true;
        }
    }
}
