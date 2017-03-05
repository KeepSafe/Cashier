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

package com.getkeepsafe.cashier;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import org.json.JSONException;
import org.json.JSONObject;

@AutoValue
public abstract class Product implements Parcelable {
    public static final String KEY_VENDOR_ID = "vendor-id";
    public static final String KEY_SKU = "sku";
    public static final String KEY_PRICE = "price";
    public static final String KEY_CURRENCY = "currency";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IS_SUB = "subscription";
    public static final String KEY_MICRO_PRICE = "micros-price";

    public abstract String vendorId();
    public abstract String sku();
    public abstract String price();
    public abstract String currency();
    public abstract String name();
    public abstract String description();
    public abstract boolean isSubscription();
    public abstract long microsPrice();

    public static Product create(String json) throws JSONException {
        return create(new JSONObject(json));
    }

    public static Product create(JSONObject json) throws JSONException {
        return create(
                json.getString(KEY_VENDOR_ID),
                json.getString(KEY_SKU),
                json.getString(KEY_PRICE),
                json.getString(KEY_CURRENCY),
                json.getString(KEY_NAME),
                json.getString(KEY_DESCRIPTION),
                json.getBoolean(KEY_IS_SUB),
                json.getLong(KEY_MICRO_PRICE));
    }

    public static Product create(String vendorId,
                                 String sku,
                                 String price,
                                 String currency,
                                 String name,
                                 String description,
                                 boolean isSubscription,
                                 long microsPrice) {
        return new AutoValue_Product(
                vendorId,
                sku,
                price,
                currency,
                name,
                description,
                isSubscription,
                microsPrice);
    }

    public JSONObject toJson() throws JSONException {
        final JSONObject object = new JSONObject();
        object.put(KEY_VENDOR_ID, vendorId());
        object.put(KEY_NAME, name());
        object.put(KEY_SKU, sku());
        object.put(KEY_PRICE, price());
        object.put(KEY_CURRENCY, currency());
        object.put(KEY_DESCRIPTION, description());
        object.put(KEY_IS_SUB, isSubscription());
        object.put(KEY_MICRO_PRICE, microsPrice());
        return object;
    }

    public String toJsonString() throws JSONException {
        return toJson().toString();
    }
}
