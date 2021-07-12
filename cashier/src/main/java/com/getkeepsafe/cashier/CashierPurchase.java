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

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class CashierPurchase implements Purchase {
  public static final String KEY_ORDER_ID = "cashier-order-id";
  public static final String KEY_TOKEN = "cashier-token";
  public static final String KEY_RECEIPT = "cashier-receipt";
  public static final String KEY_DEV_PAYLOAD = "cashier-developer-payload";
  public static final String KEY_ACCOUNT_ID = "cashier-account-id";

  private final Product product;
  private final String orderId;
  private final String token;
  private final String receipt;
  private final String developerPayload;
  private final String accountId;

  public static final Parcelable.Creator<CashierPurchase> CREATOR = new Parcelable.Creator<CashierPurchase>() {

    @Override
    public CashierPurchase createFromParcel(Parcel parcel) {
      return new CashierPurchase(
              parcel.<Product>readParcelable(Product.class.getClassLoader()),
              parcel.readString(),
              parcel.readString(),
              parcel.readString(),
              parcel.readString(),
              parcel.readString()
      );
    }

    @Override
    public CashierPurchase[] newArray(int size) {
      return new CashierPurchase[size];
    }
  };

  private CashierPurchase(Product product, String orderId, String token, String receipt, String developerPayload, String accountId) {
    this.product = product;
    this.orderId = orderId;
    this.token = token;
    this.receipt = receipt;
    this.developerPayload = developerPayload;
    this.accountId = accountId;
  }

  public Product product() {
    return product;
  }

  public String orderId() {
    return orderId;
  }

  public String token() {
    return token;
  }

  public String receipt() {
    return receipt;
  }

  public String developerPayload() {
    return developerPayload;
  }

  public String accountId() {
    return accountId;
  }

  public static CashierPurchase create(String json) throws JSONException {
    return create(new JSONObject(json));
  }

  public static CashierPurchase create(JSONObject json) throws JSONException {
    return create(Product.create(json),
        json.getString(KEY_ORDER_ID),
        json.getString(KEY_TOKEN),
        json.getString(KEY_RECEIPT),
        json.getString(KEY_DEV_PAYLOAD),
        json.getString(KEY_ACCOUNT_ID));
  }

  public static CashierPurchase create(Product product,
                                       String orderId,
                                       String token,
                                       String receipt,
                                       String developerPayload,
                                       String accountId) {
    return new CashierPurchase(product, orderId, token, receipt, developerPayload, accountId);
  }

  public JSONObject toJson() throws JSONException {
    final JSONObject object = product().toJson();
    object.put(KEY_ORDER_ID, orderId());
    object.put(KEY_TOKEN, token());
    object.put(KEY_RECEIPT, receipt());
    object.put(KEY_DEV_PAYLOAD, developerPayload());
    object.put(KEY_ACCOUNT_ID, accountId());
    return object;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeParcelable(product, 0);
    parcel.writeString(orderId);
    parcel.writeString(token);
    parcel.writeString(receipt);
    parcel.writeString(developerPayload);
    parcel.writeString(accountId);
  }
}
