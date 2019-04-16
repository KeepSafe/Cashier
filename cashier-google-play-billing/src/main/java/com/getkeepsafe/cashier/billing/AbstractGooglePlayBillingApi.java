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

import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.getkeepsafe.cashier.Preconditions;
import com.getkeepsafe.cashier.logging.Logger;

import java.util.List;

public abstract class AbstractGooglePlayBillingApi {

    private String packageName;
    GooglePlayBillingVendor vendor;
    Logger logger;

    public interface LifecycleListener {
        void initialized(boolean success);

        void disconnected();
    }

    public boolean initialize(@NonNull Context context, @NonNull GooglePlayBillingVendor vendor,
                              LifecycleListener listener, Logger logger) {
        Preconditions.checkNotNull(context, "Context is null");
        Preconditions.checkNotNull(vendor, "Vendor is null");

        this.packageName = context.getPackageName();
        this.vendor = vendor;
        this.logger = logger;
        return true;
    }

    public abstract boolean available();

    public abstract void dispose();

    public abstract int isBillingSupported(@SkuType String itemType);

    public abstract void getSkuDetails(@SkuType String itemType, @NonNull List<String> skus,
                                       @NonNull SkuDetailsResponseListener listener);

    public abstract void launchBillingFlow(@NonNull Activity activity, @NonNull String sku, @SkuType String itemType);

    @Nullable
    public abstract List<Purchase> getPurchases();

    @Nullable
    public abstract List<Purchase> getPurchases(@SkuType String itemType);

    public abstract void consumePurchase(@NonNull String purchaseToken, @NonNull ConsumeResponseListener listener);

    protected void throwIfUnavailable() {
        if (packageName == null) {
            throw new IllegalStateException("You did not specify the package name");
        }
    }
}
