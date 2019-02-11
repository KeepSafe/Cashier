/*
 *  Copyright 2017 Keepsafe Software, Inc.
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
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.getkeepsafe.cashier.Preconditions;
import com.getkeepsafe.cashier.logging.Logger;

public abstract class AbstractGooglePlayBillingApi implements PurchasesUpdatedListener {

    protected String packageName;
    private GooglePlayBillingVendor vendor;
    private Logger logger;

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

    public abstract void dispose(Context context);

    public abstract int isBillingSupported(@SkuType String itemType);

    public abstract Bundle getSkuDetails(String itemType, Bundle skus);

    public abstract void launchBillingFlow(String sku, String itemType, String developerPayload);

    public abstract Bundle getPurchases(String itemType, String paginationToken);

    public abstract int consumePurchase(String purchaseToken);

    protected void throwIfUnavailable() {
        if (packageName == null) {
            throw new IllegalStateException("You did not specify the package name");
        }
    }
}
