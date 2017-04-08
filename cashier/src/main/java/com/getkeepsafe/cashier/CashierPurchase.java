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

import com.google.auto.value.AutoValue;

import org.json.JSONException;
import org.json.JSONObject;

@AutoValue
public abstract class CashierPurchase implements Purchase {
  public static final String KEY_ORDER_ID = "cashier-order-id";
  public static final String KEY_TOKEN = "cashier-token";
  public static final String KEY_RECEIPT = "cashier-receipt";
  public static final String KEY_DEV_PAYLOAD = "cashier-developer-payload";

  public abstract Product product();

  public abstract String orderId();

  public abstract String token();

  public abstract String receipt();

  public abstract String developerPayload();

  public static CashierPurchase create(String json) throws JSONException {
    return create(new JSONObject(json));
  }

  public static CashierPurchase create(JSONObject json) throws JSONException {
    return create(Product.create(json),
        json.getString(KEY_ORDER_ID),
        json.getString(KEY_TOKEN),
        json.getString(KEY_RECEIPT),
        json.getString(KEY_DEV_PAYLOAD));
  }

  public static CashierPurchase create(Product product,
                                       String orderId,
                                       String token,
                                       String receipt,
                                       String developerPayload) {
    return new AutoValue_CashierPurchase(product, orderId, token, receipt, developerPayload);
  }

  public JSONObject toJson() throws JSONException {
    final JSONObject object = product().toJson();
    object.put(KEY_ORDER_ID, orderId());
    object.put(KEY_TOKEN, token());
    object.put(KEY_RECEIPT, receipt());
    object.put(KEY_DEV_PAYLOAD, developerPayload());
    return object;
  }
}
