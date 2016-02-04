package com.getkeepsafe.cashier.googleplay;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.getkeepsafe.cashier.ConsumeListener;
import com.getkeepsafe.cashier.Inventory;
import com.getkeepsafe.cashier.InventoryListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.PurchaseListener;
import com.getkeepsafe.cashier.Vendor;
import com.getkeepsafe.cashier.logging.Logger;
import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class InAppBillingV3Vendor implements Vendor, GooglePlayConstants {
    @Nullable
    private final String developerPayload;
    private final InAppBillingV3API api;

    @Nullable
    private Logger logger;
    private Product pendingProduct;
    private PurchaseListener purchaseListener;
    private InitializationListener initializationListener;

    private int requestCode;
    private boolean available;
    private boolean canSubscribe;
    private boolean canPurchaseItems;

    private final InAppBillingV3API.LifecycleListener lifecycleListener
            = new InAppBillingV3API.LifecycleListener() {
        @Override
        public void initialized(final boolean success) {
            if (!success) {
                logAndDisable("Couldn't create InAppBillingService instance");
                return;
            }

            try {
                canPurchaseItems = api.isBillingSupported(PRODUCT_TYPE_ITEM)
                        == BILLING_RESPONSE_RESULT_OK;

                canSubscribe = api.isBillingSupported(PRODUCT_TYPE_SUBSCRIPTION)
                        == BILLING_RESPONSE_RESULT_OK;

                available = canPurchaseItems || canSubscribe;
                log("Connected to service and it is " + (available ? "available" : "not available"));
                initializationListener.initialized();
            } catch (RemoteException e) {
                logAndDisable(Log.getStackTraceString(e));
            }
        }

        @Override
        public void disconnected() {
            logAndDisable("Disconnected from service");
        }
    };

    public InAppBillingV3Vendor(@NonNull final InAppBillingV3API api) {
        this(api, null);
    }

    public InAppBillingV3Vendor(@NonNull final InAppBillingV3API api,
                                @Nullable final String developerPayload) {
        this.api = Check.notNull(api, "API Interface");
        this.developerPayload = developerPayload;
        available = false;
    }

    @Override
    public void initialize(@NonNull final Activity activity,
                           @NonNull final InitializationListener listener) {
        Check.notNull(activity, "Activity");
        Check.notNull(listener, "Initialization Listener");
        initializationListener = listener;

        if (api.available()) {
            initializationListener.initialized();
            return;
        }

        log("Initializing In-app billing v3...");
        available = api.initialize(activity, lifecycleListener);
    }

    @Override
    public void dispose(@NonNull final Activity activity) {
        log("Disposing self...");
        Check.notNull(activity, "Activity");
        api.dispose(activity);
    }

    @Override
    public boolean available() {
        return available && api.available() && canPurchaseAnything();
    }

    @Override
    public boolean canPurchase(@NonNull final Product product) {
        Check.notNull(product, "Product");
        if (!canPurchaseAnything()) {
            return false;
        }

        if (product.isSubscription && !canSubscribe) {
            return false;
        }

        if (!product.isSubscription && !canPurchaseItems) {
            return false;
        }

        // TODO: Maybe query inventory and match skus

        return true;
    }

    @Override
    public void purchase(@NonNull final Activity activity,
                         @NonNull final Product product,
                         @NonNull final PurchaseListener listener) {
        Check.notNull(activity, "Activity");
        Check.notNull(product, "Product");
        Check.notNull(listener, "Purchase Listener");
        throwIfUninitialized();

        if (!canPurchase(product)) {
            throw new IllegalArgumentException("Cannot purchase given product!" + product.toString());
        }

        log("Constructing buy intent...");
        final String type = product.isSubscription ? PRODUCT_TYPE_SUBSCRIPTION : PRODUCT_TYPE_ITEM;
        try {
            //noinspection ConstantConditions
            final Bundle buyBundle = api.getBuyIntent(product.sku, type, developerPayload);
            final int response = getResponseCode(buyBundle);
            if (response != BILLING_RESPONSE_RESULT_OK) {
                log("Couldn't purchase product! code:" + response);
                listener.failure(product, purchaseError(response));
                return;
            }

            final PendingIntent pendingIntent = buyBundle.getParcelable(RESPONSE_BUY_INTENT);
            if (pendingIntent == null) {
                log("Received no pending intent!");
                listener.failure(product, purchaseError(response));
                return;
            }

            log("Launching buy intent for " + product.sku);
            this.purchaseListener = listener;
            pendingProduct = product;
            requestCode = new Random().nextInt(1024);
            activity.startIntentSenderForResult(pendingIntent.getIntentSender(),
                    requestCode,
                    new Intent(), 0, 0, 0);
        } catch (RemoteException | IntentSender.SendIntentException e) {
            log("Failed to launch purchase!\n" + Log.getStackTraceString(e));
            listener.failure(product, purchaseError(BILLING_RESPONSE_RESULT_ERROR));
        }
    }

    @Override
    public void consume(@NonNull final Activity activity,
                        @NonNull final Purchase purchase,
                        @NonNull final ConsumeListener listener) {
        Check.notNull(activity, "Activity");
        Check.notNull(purchase, "Purchase");
        Check.notNull(listener, "Consume Listener");
        throwIfUninitialized();

        if (purchase.isSubscription) {
            throw new IllegalArgumentException("Cannot consume a subscription!");
        }

        try {
            log("Consuming " + purchase.sku + " " + purchase.token);
            //noinspection ConstantConditions
            final int response = api.consumePurchase(purchase.token);
            if (response == BILLING_RESPONSE_RESULT_OK) {
                log("Successfully consumed purchase!");
                listener.success(purchase);
            } else {
                log("Couldn't consume purchase! " + response);
                listener.failure(purchase, consumeError(response));
            }
        } catch (RemoteException e) {
            log("Couldn't consume purchase! " + Log.getStackTraceString(e));
            listener.failure(purchase, consumeError(BILLING_RESPONSE_RESULT_ERROR));
        }
    }

    @Override
    public void getInventory(@NonNull final Activity activity,
                             @NonNull final InventoryListener listener) {
        getInventory(activity, null, null, listener);
    }

    @Override
    public void getInventory(@NonNull final Activity activity,
                             @Nullable final List<String> itemSkus,
                             @Nullable final List<String> subSkus,
                             @NonNull final InventoryListener listener) {
        Check.notNull(activity, "Activity");
        Check.notNull(listener, "Inventory Listener");
        throwIfUninitialized();

        final Inventory inventory = new Inventory();
        try {
            log("Querying inventory...");
            inventory.addPurchases(getPurchases(PRODUCT_TYPE_ITEM));
            inventory.addPurchases(getPurchases(PRODUCT_TYPE_SUBSCRIPTION));

            if (itemSkus != null && !itemSkus.isEmpty()) {
                inventory.addProducts(getProductsWithType(itemSkus, PRODUCT_TYPE_ITEM));
            }

            if (subSkus!= null && !subSkus.isEmpty()) {
                inventory.addProducts(getProductsWithType(subSkus, PRODUCT_TYPE_SUBSCRIPTION));
            }

            listener.success(inventory);
        } catch (RemoteException | ApiException e) {
            listener.failure(new Vendor.Error(INVENTORY_QUERY_FAILURE, -1));
        } catch (JSONException e) {
            listener.failure(new Vendor.Error(INVENTORY_QUERY_MALFORMED_RESPONSE, -1));
        }
    }

    private List<GooglePlayPurchase> getPurchases(@NonNull final String type)
            throws RemoteException, ApiException, JSONException {
        throwIfUninitialized();
        if (type.equals(PRODUCT_TYPE_ITEM)) {
            log("Querying item purchases...");
        } else {
            log("Querying subscription purchases...");
        }
        String paginationToken = null;
        final List<GooglePlayPurchase> purchaseList = new ArrayList<>();
        do {
            //noinspection ConstantConditions
            final Bundle purchases = api.getPurchases(type, paginationToken);

            final int response = getResponseCode(purchases);
            log("Got response: " + response);
            if (response != BILLING_RESPONSE_RESULT_OK) {
                throw new ApiException(response);
            }

            if (!purchases.containsKey(RESPONSE_INAPP_ITEM_LIST)
                    || !purchases.containsKey(RESPONSE_INAPP_PURCHASE_DATA_LIST)
                    || !purchases.containsKey(RESPONSE_INAPP_SIGNATURE_LIST)) {
                throw new ApiException(BILLING_RESPONSE_RESULT_ERROR);
            }

            final List<String> purchasedSkus
                    = purchases.getStringArrayList(RESPONSE_INAPP_ITEM_LIST);
            final List<String> purchaseDataList
                    = purchases.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST);
            final List<String> signatureList
                    = purchases.getStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST);

            if (purchasedSkus == null || purchaseDataList == null || signatureList == null) {
                return Collections.emptyList();
            }

            final List<Product> purchasedProducts = getProductsWithType(purchasedSkus, type);

            for (int i = 0; i < purchaseDataList.size(); ++i) {
                final String purchaseData = purchaseDataList.get(i);
                final String sku = purchasedSkus.get(i);
                final String signature = signatureList.get(i);
                final Product product = purchasedProducts.get(i);
                // TODO: Should make the product aligns with sku
                log("Found purchase: " + sku);

                // TODO: Security verification?
                purchaseList.add(GooglePlayPurchase.of(product, purchaseData, signature));
            }

            paginationToken = purchases.getString(INAPP_CONTINUATION_TOKEN);
            if (paginationToken != null) {
                log("Pagination token found, continuing on....");
            }
        } while (!TextUtils.isEmpty(paginationToken));

        return Collections.unmodifiableList(purchaseList);
    }

    private List<Product> getProductsWithType(@NonNull final List<String> skus,
                                              @NonNull final String type)
            throws RemoteException, ApiException {
        throwIfUninitialized();
        Check.notNull(skus, "SKU list");
        Check.notNull(type, "Product type");
        log("Retrieving sku details for " + skus.size() + " " + type + " skus");
        if (!type.equals(PRODUCT_TYPE_ITEM) && !type.equals(PRODUCT_TYPE_SUBSCRIPTION)) {
            throw new IllegalArgumentException("Invalid product type " + type);
        }

        final List<Product> products = new ArrayList<>();
        for (int i = 0; i < skus.size(); i += 20) {
            final ArrayList<String> page
                    // Terrible un-needed forced cast because of bundle api
                    = new ArrayList<>(skus.subList(i, Math.min(skus.size(), i + 20)));
            final Bundle skuQuery = new Bundle();
            skuQuery.putStringArrayList(REQUEST_SKU_DETAILS_ITEM_LIST, page);

            //noinspection ConstantConditions
            final Bundle skuDetails = api.getSkuDetails(type, skuQuery);

            final int response = getResponseCode(skuDetails);
            log("Got response: " + response);
            if (response != BILLING_RESPONSE_RESULT_OK
                    || !skuDetails.containsKey(RESPONSE_GET_SKU_DETAILS_LIST)) {
                throw new ApiException(response);
            }

            final ArrayList<String> detailsList
                    = skuDetails.getStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST);
            if (detailsList == null) continue;

            for (final String detail : detailsList) {
                log("Parsing sku details: " + detail);
                try {
                    products.add(GooglePlayProduct.of(detail, type.equals(PRODUCT_TYPE_SUBSCRIPTION)));
                } catch (JSONException e) {
                    log("Couldn't parse sku: " + detail);
                }
            }
        }

        return Collections.unmodifiableList(products);
    }

    @Override
    public void setLogger(@Nullable final Logger logger) {
        this.logger = logger;
        if (this.logger != null) {
            this.logger.setTag("InAppBillingV3");
        }
    }

    @Override
    public boolean onActivityResult(final int requestCode,
                                    final int resultCode,
                                    final Intent data) {
        log("onActivityResult " + resultCode);
        if (this.requestCode != requestCode) {
            return false;
        }

        if (data == null) {
            purchaseListener.failure(pendingProduct, purchaseError(BILLING_RESPONSE_RESULT_ERROR));
            return true;
        }

        final int responseCode = getResponseCode(data);
        if (resultCode == Activity.RESULT_OK && responseCode == BILLING_RESPONSE_RESULT_OK) {
            log("Successful purchase of " + pendingProduct.sku + "!");

            try {
                purchaseListener.success(GooglePlayPurchase.of(pendingProduct, data));
            } catch (JSONException e) {
                purchaseListener.failure(pendingProduct,
                        new Vendor.Error(Vendor.PURCHASE_SUCCESS_RESULT_MALFORMED,
                                BILLING_RESPONSE_RESULT_ERROR));
            }
        } else if (resultCode == Activity.RESULT_OK) {
            log("Purchase failed! " + responseCode);
            purchaseListener.failure(pendingProduct, purchaseError(responseCode));
        } else {
            log("Purchase canceled! " + responseCode);
            purchaseListener.failure(pendingProduct, purchaseError(responseCode));
        }

        return true;
    }

    private void throwIfUninitialized() {
        if (!api.available()) {
            throw new IllegalStateException("Trying to purchase without initializing first!");
        }
    }

    private boolean canPurchaseAnything() {
        return canPurchaseItems || canSubscribe;
    }

    private void logAndDisable(@NonNull final String message) {
        log(message);
        available = false;
    }

    private int getResponseCode(@NonNull final Intent intent) {
        final Bundle extras = intent.getExtras();
        return getResponseCode(extras);
    }

    private int getResponseCode(@Nullable final Bundle bundle) {
        if (bundle == null) {
            log("Null response code from bundle, assuming OK (known issue)");
            return BILLING_RESPONSE_RESULT_OK;
        }

        final Object o = bundle.get(RESPONSE_CODE);
        if (o == null) {
            log("Null response code from bundle, assuming OK (known issue)");
            return BILLING_RESPONSE_RESULT_OK;
        } else if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof Long) {
            return ((Long) o).intValue();
        } else {
            final String message
                    = "Unexpected type for bundle response code. " + o.getClass().getName();
            log(message);
            throw new RuntimeException(message);
        }
    }

    private Vendor.Error purchaseError(final int response) {
        final int code;
        switch (response) {
            case BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
            case BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
                code = Vendor.PURCHASE_UNAVAILABLE;
                break;
            case BILLING_RESPONSE_RESULT_USER_CANCELED:
                code = Vendor.PURCHASE_CANCELED;
                break;
            case BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED:
                code = Vendor.PURCHASE_ALREADY_OWNED;
                break;
            case BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED:
                code = Vendor.PURCHASE_NOT_OWNED;
                break;
            case BILLING_RESPONSE_RESULT_DEVELOPER_ERROR:
            case BILLING_RESPONSE_RESULT_ERROR:
            default:
                code = Vendor.PURCHASE_FAILURE;
                break;
        }

        return new Vendor.Error(code, response);
    }

    private Vendor.Error consumeError(final int response) {
        final int code;
        switch (response) {
            case BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
            case BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
                code = Vendor.CONSUME_UNAVAILABLE;
                break;
            case BILLING_RESPONSE_RESULT_USER_CANCELED:
                code = Vendor.CONSUME_CANCELED;
                break;
            case BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED:
                code = Vendor.CONSUME_NOT_OWNED;
                break;
            case BILLING_RESPONSE_RESULT_DEVELOPER_ERROR:
            case BILLING_RESPONSE_RESULT_ERROR:
            default:
                code = Vendor.CONSUME_FAILURE;
                break;
        }

        return new Vendor.Error(code, response);
    }

    private void log(@NonNull final String message) {
        if (logger == null) return;
        logger.log(message);
    }

    private class ApiException extends Exception {
        private final int code;
        public ApiException(final int code) {
            super("Received Billing API response " + code);
            this.code = code;
        }

        public int code() {
            return code;
        }
    }
}
