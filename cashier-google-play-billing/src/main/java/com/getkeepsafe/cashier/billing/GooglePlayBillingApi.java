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
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.android.billingclient.api.Purchase;

import java.util.List;

public final class GooglePlayBillingApi extends AbstractGooglePlayBillingApi {

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public void dispose(Context context) {
    }

    @Override
    public int isBillingSupported(String itemType) {
        return 0;
    }

    @Override
    public Bundle getSkuDetails(String itemType, Bundle skus) {
        return null;
    }

    @Override
    public void launchBillingFlow(String sku, String itemType, String developerPayload) {
    }

    @Override
    public Bundle getPurchases(String itemType, String paginationToken) {
        return null;
    }

    @Override
    public int consumePurchase(String purchaseToken) {
        return 0;
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
    }
}
