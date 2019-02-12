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

import com.android.billingclient.api.BillingClient.BillingResponse;
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

import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.VENDOR_PACKAGE;

public final class GooglePlayBillingVendor implements Vendor, SkuDetailsResponseListener, PurchasesUpdatedListener,
        ConsumeResponseListener {

    @Override
    public String id() {
        return VENDOR_PACKAGE;
    }

    @Override
    public void initialize(Context context, InitializationListener listener) {
    }

    @Override
    public void dispose(Context context) {
    }

    @Override
    public void purchase(Activity activity, Product product, String developerPayload, PurchaseListener listener) {

    }

    @Override
    public void onPurchasesUpdated(@BillingResponse int responseCode,
                                   @Nullable List<com.android.billingclient.api.Purchase> purchases) {
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
    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
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
}
