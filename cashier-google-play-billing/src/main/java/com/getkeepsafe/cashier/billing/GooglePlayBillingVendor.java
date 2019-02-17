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
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.getkeepsafe.cashier.ConsumeListener;
import com.getkeepsafe.cashier.InventoryListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.ProductDetailsListener;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.PurchaseListener;
import com.getkeepsafe.cashier.Vendor;
import com.getkeepsafe.cashier.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.*;
import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.VENDOR_PACKAGE;

public final class GooglePlayBillingVendor implements Vendor, SkuDetailsResponseListener, PurchasesUpdatedListener,
        ConsumeResponseListener, AbstractGooglePlayBillingApi.LifecycleListener {

    /** Internal log tag **/
    private static final String LOG_TAG = "GoogleBillingVendor";
    /** Google Play Billing API wrapper **/
    private final AbstractGooglePlayBillingApi api;
    /** Google Play Billing API key **/
    private final String publicKey64;

    private Logger logger;
    private Product pendingProduct;
    private PurchaseListener purchaseListener;
    private InitializationListener initializationListener;

    private boolean available = false;
    private boolean canSubscribe = false;
    private boolean canPurchaseItems = false;

    public GooglePlayBillingVendor() {
        this(new GooglePlayBillingApi(), null);
    }

    public GooglePlayBillingVendor(String publicKey64) {
        this(new GooglePlayBillingApi(), publicKey64);
    }

    public GooglePlayBillingVendor(AbstractGooglePlayBillingApi api) {
        this(api, null);
    }

    public GooglePlayBillingVendor(AbstractGooglePlayBillingApi api, @Nullable String publicKey64) {
        if (api == null) {
            throw new IllegalArgumentException("Cannot initialize will null api...");
        }

        this.api = api;
        this.publicKey64 = publicKey64;
        available = false;
    }

    @Override
    public String id() {
        return VENDOR_PACKAGE;
    }

    @Override
    public void initialize(Context context, InitializationListener listener) {
        if (context == null) {
            throw new IllegalStateException("Cannot initialize with null context");
        }

        if (listener == null) {
            throw new IllegalArgumentException("Cannot initialize with null initialization listener");
        }

        initializationListener = listener;

        if (available()) {
            listener.initialized();
            return;
        }

        logSafely("Initializing Google Play Billing API...");
        available = api.initialize(context, this, this, logger);

        if (!available) {
            initializationListener.unavailable();
        }
    }

    @Override
    public void initialized(boolean success) {
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
            initializationListener.initialized();
        } catch (Exception error) {
            logAndDisable(Log.getStackTraceString(error));
        }
    }

    @Override
    public void disconnected() {
        logAndDisable("Disconnected from Google Play Billing service.");
    }

    @Override
    public void dispose(Context context) {
        logSafely("Disposing Google Play Billing vendor...");
        api.dispose();
        available = false;
    }

    @Override
    public void purchase(Activity activity, Product product, String developerPayload, PurchaseListener listener) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity is null.");
        }

        if (product == null) {
            throw new IllegalArgumentException("Product is null.");
        }

        if (listener == null) {
            throw new IllegalArgumentException("Listener is null.");
        }

        this.purchaseListener = listener;
        this.pendingProduct = product;
        logSafely("Launching Google Play Billing flow for " + product.sku());
        api.launchBillingFlow(activity, product.sku(), product.isSubscription() ? SkuType.SUBS : SkuType.INAPP);
    }

    @Override
    public void onPurchasesUpdated(@BillingResponse int responseCode,
                                   @Nullable List<com.android.billingclient.api.Purchase> purchases) {
        switch (responseCode) {
            case BillingResponse.OK:
                return;
            case BillingResponse.USER_CANCELED:
                purchaseListener.failure(pendingProduct, new Error(BILLING_RESPONSE_RESULT_USER_CANCELED, responseCode));
                return;
            default:
                break;
        }
    }

    @Override
    public void consume(Context context, Purchase purchase, ConsumeListener listener) {
    }

    @Override
    public void onConsumeResponse(@BillingResponse int responseCode, String purchaseToken) {
    }

    @Override
    public void getInventory(Context context, Collection<String> itemSkus, Collection<String> subSkus,
                             InventoryListener listener) {
    }

    @Override
    public void getProductDetails(Context context, String sku, boolean isSubscription,
                                  ProductDetailsListener listener) {
    }

    @Override
    public void onSkuDetailsResponse(@BillingResponse int responseCode, List<SkuDetails> skuDetailsList) {
    }

    @Override
    public void setLogger(Logger logger) {
    }

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public boolean canPurchase(Product product) {
        return false;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        // Do nothing, {@link #onPurchasesUpdated} will be called for billing flow.
        return false;
    }

    @Override
    public Product getProductFrom(JSONObject json) throws JSONException {
        return null;
    }

    @Override
    public Purchase getPurchaseFrom(JSONObject json) throws JSONException {
        return null;
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
}
