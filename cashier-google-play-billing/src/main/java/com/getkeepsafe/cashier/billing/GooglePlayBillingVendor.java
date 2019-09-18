/*
 *  Copyright 2019 Keepsafe Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.getkeepsafe.cashier.billing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.getkeepsafe.cashier.ConsumeListener;
import com.getkeepsafe.cashier.InventoryListener;
import com.getkeepsafe.cashier.Preconditions;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.ProductDetailsListener;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.PurchaseListener;
import com.getkeepsafe.cashier.Vendor;
import com.getkeepsafe.cashier.VendorConstants;
import com.getkeepsafe.cashier.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.getkeepsafe.cashier.VendorConstants.CONSUME_CANCELED;
import static com.getkeepsafe.cashier.VendorConstants.CONSUME_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.CONSUME_NOT_OWNED;
import static com.getkeepsafe.cashier.VendorConstants.CONSUME_UNAVAILABLE;
import static com.getkeepsafe.cashier.VendorConstants.PRODUCT_DETAILS_QUERY_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.PRODUCT_DETAILS_UNAVAILABLE;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_ALREADY_OWNED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_CANCELED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_NOT_OWNED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_SUCCESS_RESULT_MALFORMED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_UNAVAILABLE;
import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.VENDOR_PACKAGE;

public final class GooglePlayBillingVendor implements Vendor, PurchasesUpdatedListener,
        AbstractGooglePlayBillingApi.LifecycleListener {

    /**
     * Internal log tag
     **/
    private static final String LOG_TAG = "GoogleBillingVendor";

    /**
     * Google Play Billing API wrapper
     **/
    private final AbstractGooglePlayBillingApi api;

    /**
     * Google Play Billing API key
     **/
    private final String publicKey64;

    private Logger logger;

    /**
     * Product being purchased. If not null, purchase is in progress.
     */
    private Product pendingProduct;

    /**
     * Pending purchase listener.
     */
    private PurchaseListener purchaseListener;

    /**
     * Initialization listeners. Initialization may be called from more than one thread simultaneously.
     */
    private List<InitializationListener> initializationListeners = new ArrayList<>();

    private boolean available = false;
    private boolean initializing = false;
    private boolean canSubscribe = false;
    private boolean canPurchaseItems = false;

    /**
     * Tokens to be consumed. Contains token being currently consumed or already consumed.
     */
    private Set<String> tokensToBeConsumed = new HashSet<>();

    public GooglePlayBillingVendor() {
        this(new GooglePlayBillingApi(), null);
    }

    /**
     * @param publicKey64 should be YOUR APPLICATION'S PUBLIC KEY
     * (that you got from the Google Play developer console). This is not your
     * developer public key, it's the *app-specific* public key.
     */
    public GooglePlayBillingVendor(String publicKey64) {
        this(new GooglePlayBillingApi(), publicKey64);
    }

    public GooglePlayBillingVendor(AbstractGooglePlayBillingApi api) {
        this(api, null);
    }

    public GooglePlayBillingVendor(AbstractGooglePlayBillingApi api, @Nullable String publicKey64) {
        Preconditions.checkNotNull(api, "Cannot initialize will null api...");

        this.api = api;
        this.publicKey64 = publicKey64;
        this.available = false;
    }

    @Override
    public String id() {
        return VENDOR_PACKAGE;
    }

    @Override
    public synchronized void initialize(Context context, InitializationListener listener) {
        Preconditions.checkNotNull(context, "Cannot initialize with null context");
        Preconditions.checkNotNull(listener, "Cannot initialize with null initialization listener");

        if (available()) {
            listener.initialized();
            return;
        }

        initializationListeners.add(listener);

        if (!initializing) {
            initializing = true;
            logSafely("Initializing Google Play Billing API...");
            available = api.initialize(context, this, this, logger);
        }

        if (!available) {
            initializationListeners.remove(listener);
            listener.unavailable();
        }
    }

    @Override
    public synchronized void initialized(boolean success) {
        logSafely("Initialized: success = " + success);
        if (!success) {
            logAndDisable("Could not create Google Play Billing instance");
            return;
        }

        try {
            canPurchaseItems =
                    api.isBillingSupported(SkuType.INAPP) == BillingResponse.OK;

            canSubscribe =
                    api.isBillingSupported(SkuType.SUBS) == BillingResponse.OK;

            available = canPurchaseItems || canSubscribe;
            logSafely("Connected to service and it is " + (available ? "available" : "not available"));
            initializing = false;

            for (InitializationListener listener : initializationListeners) {
                listener.initialized();
            }
            initializationListeners.clear();
        } catch (Exception error) {
            logAndDisable(Log.getStackTraceString(error));
        }
    }

    @Override
    public void disconnected() {
        logAndDisable("Disconnected from Google Play Billing service.");
        for (InitializationListener listener : initializationListeners) {
            listener.unavailable();
        }
        initializationListeners.clear();
    }

    @Override
    public void dispose(Context context) {
        logSafely("Disposing Google Play Billing vendor...");
        api.dispose();
        available = false;
        initializationListeners.clear();
    }

    @Override
    public synchronized void purchase(Activity activity, Product product, String developerPayload, PurchaseListener listener) {
        Preconditions.checkNotNull(activity, "Activity is null.");
        Preconditions.checkNotNull(product, "Product is null.");
        Preconditions.checkNotNull(listener, "Purchase listener is null.");
        throwIfUninitialized();

        if (pendingProduct != null) {
            throw new RuntimeException("Cannot purchase product while another purchase is in progress!");
        }

        // NOTE: Developer payload is not supported with Google Play Billing
        // https://issuetracker.google.com/issues/63381481
        if (developerPayload != null && developerPayload.length() > 0) {
            throw new RuntimeException("Developer payload is not supported in Google Play Billing!");
        }

        this.purchaseListener = listener;
        this.pendingProduct = product;
        logSafely("Launching Google Play Billing flow for " + product.sku());
        try {
            api.launchBillingFlow(activity, product.sku(), product.isSubscription() ? SkuType.SUBS : SkuType.INAPP);
        } catch (Exception e) {
            clearPendingPurchase();
            throw e;
        }
    }

    @Override
    public void onPurchasesUpdated(@BillingResponse int responseCode,
                                   @Nullable List<com.android.billingclient.api.Purchase> purchases) {
        if (purchaseListener == null) {
            pendingProduct = null;
            logSafely("#onPurchasesUpdated called but no purchase listener attached.");
            return;
        }

        switch (responseCode) {
            case BillingResponse.OK:
                if (purchases == null || purchases.isEmpty()) {
                    purchaseListener.failure(pendingProduct, new Error(PURCHASE_FAILURE, responseCode));
                    clearPendingPurchase();
                    return;
                }

                for (com.android.billingclient.api.Purchase purchase : purchases) {
                    handlePurchase(purchase, responseCode);
                }
                return;
            case BillingResponse.USER_CANCELED:
                logSafely("User canceled the purchase code: " + responseCode);
                purchaseListener.failure(pendingProduct, getPurchaseError(responseCode));
                clearPendingPurchase();
                return;
            default:
                logSafely("Error purchasing item with code: " + responseCode);
                purchaseListener.failure(pendingProduct, getPurchaseError(responseCode));
                clearPendingPurchase();
        }
    }

    private void handlePurchase(com.android.billingclient.api.Purchase purchase, int responseCode) {
        // Convert Billing Client purchase model to internal Cashier purchase model
        try {
            Purchase cashierPurchase = GooglePlayBillingPurchase.create(pendingProduct, purchase);

            // Check data signature matched with specified public key
            if (!TextUtils.isEmpty(publicKey64)
                    && !GooglePlayBillingSecurity.verifySignature(publicKey64,
                    purchase.getOriginalJson(), purchase.getSignature())) {
                logSafely("Local signature check failed!");
                purchaseListener.failure(pendingProduct, new Error(PURCHASE_SUCCESS_RESULT_MALFORMED, responseCode));
                clearPendingPurchase();
                return;
            }

            logSafely("Successful purchase of " + purchase.getSku() + "!");
            purchaseListener.success(cashierPurchase);
            clearPendingPurchase();
        } catch (JSONException error) {
            logSafely("Error in parsing purchase response: " + purchase.getSku());
            purchaseListener.failure(pendingProduct, new Error(PURCHASE_SUCCESS_RESULT_MALFORMED, responseCode));
            clearPendingPurchase();
        }
    }

    private void clearPendingPurchase() {
        pendingProduct = null;
        purchaseListener = null;
    }

    @Override
    public synchronized void consume(@NonNull final Context context, @NonNull final Purchase purchase, @NonNull final ConsumeListener listener) {
        Preconditions.checkNotNull(context, "Purchase is null");
        Preconditions.checkNotNull(listener, "Consume listener is null");
        throwIfUninitialized();

        final Product product = purchase.product();
        if (product.isSubscription()) {
            throw new IllegalStateException("Cannot consume a subscription");
        }

        if (tokensToBeConsumed.contains(purchase.token())) {
            // Purchase currently being consumed or already successfully consumed.
            logSafely("Token was already scheduled to be consumed - skipping...");
            listener.failure(purchase, new Error(VendorConstants.CONSUME_UNAVAILABLE, -1));
            return;
        }

        logSafely("Consuming " + product.sku());
        tokensToBeConsumed.add(purchase.token());

        api.consumePurchase(purchase.token(), new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(int responseCode, String purchaseToken) {
                if (responseCode == BillingResponse.OK) {
                    logSafely("Successfully consumed " + purchase.product().sku() + "!");
                    listener.success(purchase);
                } else {
                    // Failure in consuming token, remove from the list so retry is possible
                    logSafely("Error consuming " + purchase.product().sku() + " with code "+responseCode);
                    tokensToBeConsumed.remove(purchaseToken);
                    listener.failure(purchase, getConsumeError(responseCode));
                }
            }
        });
    }

    @Override
    public void getInventory(@NonNull Context context, @Nullable Collection<String> itemSkus, @Nullable Collection<String> subSkus,
                             @NonNull InventoryListener listener) {
        throwIfUninitialized();

        logSafely("Getting inventory ...");
        InventoryQuery.execute(api, listener, itemSkus, subSkus);
    }

    @Override
    public void getProductDetails(@NonNull Context context, @NonNull final String sku, final boolean isSubscription,
                                  @NonNull final ProductDetailsListener listener) {
        throwIfUninitialized();

        api.getSkuDetails(
                isSubscription ? SkuType.SUBS : SkuType.INAPP,
                Collections.singletonList(sku),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                        if (responseCode == BillingResponse.OK && skuDetailsList.size() == 1) {
                            logSafely("Successfully got sku details for " + sku + "!");
                            listener.success(
                                    GooglePlayBillingProduct.create(skuDetailsList.get(0), isSubscription ? SkuType.SUBS : SkuType.INAPP)
                            );
                        } else {
                            logSafely("Error getting sku details for " + sku + " with code "+responseCode);
                            listener.failure(getDetailsError(responseCode));
                        }
                    }
                }
        );
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean available() {
        return available && api.available() && canPurchaseAnything();
    }

    @Override
    public boolean canPurchase(Product product) {
        if (!canPurchaseAnything()) {
            return false;
        }

        if (product.isSubscription() && !canSubscribe) {
            return false;
        }

        if (!product.isSubscription() && !canPurchaseItems) {
            return false;
        }

        return true;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        // Do nothing, {@link #onPurchasesUpdated} will be called for billing flow.
        return false;
    }

    @Override
    public Product getProductFrom(JSONObject json) throws JSONException {
        // NOTE: This is not needed for the Google Play Billing Vendor
        throw new UnsupportedOperationException("This is not supported with Google Play Billing Vendor.");
    }

    @Override
    public Purchase getPurchaseFrom(JSONObject json) throws JSONException {
        // NOTE: This is not needed for the Google Play Billing Vendor
        throw new UnsupportedOperationException("This is not supported with Google Play Billing Vendor.");
    }

    private boolean canPurchaseAnything() {
        return canPurchaseItems || canSubscribe;
    }

    private void logSafely(String message) {
        if (logger == null || message == null) {
            return;
        }

        logger.i(LOG_TAG, message);
    }

    private void logAndDisable(String message) {
        logSafely(message);
        available = false;
    }

    private Error getPurchaseError(int responseCode) {
        final int code;
        switch (responseCode) {
            case BillingResponse.FEATURE_NOT_SUPPORTED:
            case BillingResponse.SERVICE_DISCONNECTED:
            case BillingResponse.SERVICE_UNAVAILABLE:
            case BillingResponse.BILLING_UNAVAILABLE:
            case BillingResponse.ITEM_UNAVAILABLE:
                code = PURCHASE_UNAVAILABLE;
                break;
            case BillingResponse.USER_CANCELED:
                code = PURCHASE_CANCELED;
                break;
            case BillingResponse.ITEM_ALREADY_OWNED:
                code = PURCHASE_ALREADY_OWNED;
                break;
            case BillingResponse.ITEM_NOT_OWNED:
                code = PURCHASE_NOT_OWNED;
                break;
            case BillingResponse.DEVELOPER_ERROR:
            case BillingResponse.ERROR:
            default:
                code = PURCHASE_FAILURE;
                break;
        }

        return new Error(code, responseCode);
    }

    private Error getConsumeError(int responseCode) {
        final int code;
        switch (responseCode) {
            case BillingResponse.FEATURE_NOT_SUPPORTED:
            case BillingResponse.SERVICE_DISCONNECTED:
            case BillingResponse.BILLING_UNAVAILABLE:
            case BillingResponse.ITEM_UNAVAILABLE:
                code = CONSUME_UNAVAILABLE;
                break;
            case BillingResponse.USER_CANCELED:
                code = CONSUME_CANCELED;
                break;
            case BillingResponse.ITEM_NOT_OWNED:
                code = CONSUME_NOT_OWNED;
                break;
            case BillingResponse.DEVELOPER_ERROR:
            case BillingResponse.ERROR:
            default:
                code = CONSUME_FAILURE;
                break;
        }

        return new Error(code, responseCode);
    }

    private Error getDetailsError(int responseCode) {
        final int code;
        switch (responseCode) {
            case BillingResponse.FEATURE_NOT_SUPPORTED:
            case BillingResponse.SERVICE_DISCONNECTED:
            case BillingResponse.SERVICE_UNAVAILABLE:
            case BillingResponse.BILLING_UNAVAILABLE:
            case BillingResponse.ITEM_UNAVAILABLE:
                code = PRODUCT_DETAILS_UNAVAILABLE;
                break;
            case BillingResponse.USER_CANCELED:
            case BillingResponse.ITEM_NOT_OWNED:
            case BillingResponse.DEVELOPER_ERROR:
            case BillingResponse.ERROR:
            default:
                code = PRODUCT_DETAILS_QUERY_FAILURE;
                break;
        }

        return new Error(code, responseCode);
    }

    private void throwIfUninitialized() {
        if (!api.available()) {
            throw new IllegalStateException("Trying to do operation without initialized billing API");
        }
    }
}
