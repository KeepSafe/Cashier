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

package com.getkeepsafe.cashier.iab;

import com.getkeepsafe.cashier.Product;

import org.json.JSONException;
import org.json.JSONObject;

import static com.getkeepsafe.cashier.iab.InAppBillingConstants.ProductConstants.CURRENCY;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.ProductConstants.DESCRIPTION;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.ProductConstants.NAME;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.ProductConstants.PRICE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.ProductConstants.PRICE_MICRO;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.ProductConstants.SKU;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.VENDOR_PACKAGE;

public class InAppBillingProduct {
    public static Product create(String skuDetailJson, boolean isSubscription) throws JSONException {
        final JSONObject json = new JSONObject(skuDetailJson);
        return create(json.getString(SKU),
                json.getString(PRICE),
                json.getString(CURRENCY),
                json.getString(NAME),
                json.getString(DESCRIPTION),
                isSubscription,
                Long.parseLong(json.getString(PRICE_MICRO)));
    }

    public static Product create(String sku,
                                 String price,
                                 String currency,
                                 String name,
                                 String description,
                                 boolean isSubscription,
                                 long microsPrice) {
        return Product.create(
                VENDOR_PACKAGE,
                sku,
                price,
                currency,
                name,
                description,
                isSubscription,
                microsPrice);
    }

    private InAppBillingProduct() {}
}
