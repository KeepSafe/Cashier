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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClient.FeatureType;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.getkeepsafe.cashier.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public final class GooglePlayBillingApi extends AbstractGooglePlayBillingApi implements BillingClientStateListener {
    /** Internal log tag **/
    private static final String LOG_TAG = "GoogleBillingApi";
    /** Google Play Billing client **/
    private BillingClient billing;
    /** Google Play Billing service life cycle listener **/
    private LifecycleListener listener;
    /** Google Play Billing connection state **/
    private boolean isServiceConnected = false;

    @Override
    public boolean initialize(@NonNull Context context, @NonNull GooglePlayBillingVendor vendor,
                              LifecycleListener listener, Logger logger) {
        final boolean initialized = super.initialize(context, vendor, listener, logger);
        this.listener = listener;

        if (available() && listener != null) {
            listener.initialized(true);
            return true;
        }

        logSafely("Creating Google Play Billing client...");
        billing = BillingClient.newBuilder(context)
                .setListener(vendor)
                .build();

        logSafely("Attempting to connect to billing service...");
        billing.startConnection(this);

        return initialized;
    }

    @Override
    public boolean available() {
        // Billing API is available if we are connected to the service and
        // the billing client is ready (See: BillingClient#isReady)
        return billing != null && isServiceConnected && billing.isReady();
    }

    @Override
    public void dispose() {
        logSafely("Disposing billing client.");

        if (available()) {
            billing.endConnection();
            billing = null;
        }
    }

    @Override
    public int isBillingSupported(@SkuType String itemType) {
        throwIfUnavailable();

        if (SkuType.INAPP.equalsIgnoreCase(itemType) && billing.isReady()) {
            return BillingResponse.OK;
        } else {
            return billing.isFeatureSupported(itemType);
        }
    }

    @Override
    public void getSkuDetails(@SkuType String itemType, @NonNull List<String> skus,
                              @NonNull SkuDetailsResponseListener listener) {
        throwIfUnavailable();

        logSafely("Query for SKU details with type: " + itemType + " SKUs: " + TextUtils.join(",", skus));

        SkuDetailsParams query = SkuDetailsParams.newBuilder()
                .setSkusList(skus)
                .setType(itemType)
                .build();
        billing.querySkuDetailsAsync(query, listener);
    }

    @Override
    public void launchBillingFlow(@NonNull Activity activity, @NonNull String sku, @SkuType String itemType) {
        throwIfUnavailable();
        logSafely("Launching billing flow for " + sku + " with type " + itemType);

        // TODO: Version 1.2 actually recommends using {@link BillingFlowParams#setSkuDetails(SkuDetails)}
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSku(sku)
                .setType(itemType)
                .build();

        // This will call the {@link PurchasesUpdatedListener} specified in {@link #initialize}
        billing.launchBillingFlow(activity, billingFlowParams);
    }

    @Nullable
    @Override
    public List<Purchase> getPurchases() {
        throwIfUnavailable();

        List<Purchase> allPurchases = new ArrayList<>();

        logSafely("Querying in-app purchases...");
        Purchase.PurchasesResult inAppPurchasesResult = billing.queryPurchases(SkuType.INAPP);

        if (inAppPurchasesResult.getResponseCode() == BillingResponse.OK) {
            List<Purchase> inAppPurchases = inAppPurchasesResult.getPurchasesList();
            logSafely("In-app purchases: " + TextUtils.join(", ", inAppPurchases));
            allPurchases.addAll(inAppPurchases);
            // Check if we support subscriptions and query those purchases as well
            boolean isSubscriptionSupported =
                    billing.isFeatureSupported(FeatureType.SUBSCRIPTIONS) == BillingResponse.OK;
            if (isSubscriptionSupported) {
                logSafely("Querying subscription purchases...");
                Purchase.PurchasesResult subscriptionPurchasesResult = billing.queryPurchases(SkuType.SUBS);

                if (subscriptionPurchasesResult.getResponseCode() == BillingResponse.OK) {
                    List<Purchase> subscriptionPurchases = subscriptionPurchasesResult.getPurchasesList();
                    logSafely("Subscription purchases: " + TextUtils.join(", ", subscriptionPurchases));
                    allPurchases.addAll(subscriptionPurchases);
                    return allPurchases;
                } else {
                    logSafely("Error in querying subscription purchases with code: "
                            + subscriptionPurchasesResult.getResponseCode());
                    return allPurchases;
                }
            } else {
                logSafely("Subscriptions are not supported...");
                return allPurchases;
            }
        } else {
            return null;
        }
    }

    @Override
    public void consumePurchase(@NonNull String purchaseToken, @NonNull ConsumeResponseListener listener) {
        throwIfUnavailable();

        logSafely("Consuming product with purchase token: " + purchaseToken);
        billing.consumeAsync(purchaseToken, listener);
    }

    @Override
    public void onBillingSetupFinished(@BillingResponse int billingResponseCode) {
        logSafely("Service setup finished and connected. Response: " + billingResponseCode);

        if (billingResponseCode == BillingResponse.OK) {
            isServiceConnected = true;

            if (listener != null) {
                listener.initialized(available());
            }
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        logSafely("Service disconnected");

        isServiceConnected = false;

        if (listener != null) {
            listener.disconnected();
        }
    }

    @Override
    protected void throwIfUnavailable() {
        super.throwIfUnavailable();
        if (!available()) {
            throw new IllegalStateException("Billing client is not available");
        }
    }

    private void logSafely(String message) {
        if (logger == null || message == null) {
            return;
        }

        logger.i(LOG_TAG, message);
    }
}
