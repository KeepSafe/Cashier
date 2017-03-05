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

package com.getkeepsafe.cashier.iab.debug;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;

import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.iab.AbstractInAppBillingV3API;
import com.getkeepsafe.cashier.iab.InAppBillingPurchase;

public class FakeInAppBillingV3Api extends AbstractInAppBillingV3API {
    public static final String TEST_PUBLIC_KEY = null;
    
    public static void addTestProduct(Product product) {
        // no-op
    }

    public static void addTestPurchase(InAppBillingPurchase purchase) {
        // no-op
    }

    public FakeInAppBillingV3Api(Context context) {
        throw new AssertionError();
    }

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public void dispose(Context context) {
        // no-op
    }

    @Override
    public int isBillingSupported(String itemType) throws RemoteException {
        return -1;
    }

    @Override
    public Bundle getSkuDetails(String itemType, Bundle skus) throws RemoteException {
        return null;
    }

    @Override
    public Bundle getBuyIntent(String sku, String itemType, String developerPayload) throws RemoteException {
        return null;
    }

    @Override
    public Bundle getPurchases(String itemType, String paginationToken) throws RemoteException {
        return null;
    }

    @Override
    public int consumePurchase(String purchaseToken) throws RemoteException {
        return -1;
    }
}
