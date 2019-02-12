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

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.getkeepsafe.cashier.logging.Logger;

import java.util.List;

public final class GooglePlayBillingApi extends AbstractGooglePlayBillingApi implements BillingClientStateListener {
    /** Internal log tag **/
    private static final String LOG_TAG = "CashierGoogleBilling";
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

        if (logger != null) {
            logger.i(LOG_TAG, "Creating Google Play Billing client...");
        }

        billing = BillingClient.newBuilder(context)
                .setListener(vendor)
                .build();

        if (logger != null) {
            logger.i(LOG_TAG, "Attempting to connect to billing service...");
        }
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
        if (logger != null) {
            logger.i(LOG_TAG, "Disposing billing client.");
        }

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
    public void getSkuDetails(@SkuType String itemType, List<String> skus) {
        throwIfUnavailable();
    }

    @Override
    public void launchBillingFlow(String sku, String itemType, String developerPayload) {
        throwIfUnavailable();
    }

    @Override
    public void getPurchases(String itemType, String paginationToken) {
        throwIfUnavailable();
    }

    @Override
    public void consumePurchase(String purchaseToken) {
        throwIfUnavailable();
    }

    @Override
    public void onBillingSetupFinished(@BillingResponse int billingResponseCode) {
        if (logger != null) {
            logger.i(LOG_TAG, "Service setup finished and connected. Response: " + billingResponseCode);
        }

        if (billingResponseCode == BillingResponse.OK) {
            isServiceConnected = true;

            if (listener != null) {
                listener.initialized(available());
            }
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        if (logger != null) {
            logger.i(LOG_TAG, "Service disconnected");
        }

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
}
