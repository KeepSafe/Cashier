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

public class Product implements Parcelable {
  public static final String KEY_VENDOR_ID = "vendor-id";
  public static final String KEY_SKU = "sku";
  public static final String KEY_PRICE = "price";
  public static final String KEY_CURRENCY = "currency";
  public static final String KEY_NAME = "name";
  public static final String KEY_DESCRIPTION = "description";
  public static final String KEY_IS_SUB = "subscription";
  public static final String KEY_MICRO_PRICE = "micros-price";

  private final String vendorId;
  private final String sku;
  private final String price;
  private final String currency;
  private final String name;
  private final String description;
  private final boolean isSubscription;
  private final long microsPrice;

  public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {

    @Override
    public Product createFromParcel(Parcel parcel) {
      return new Product(
              parcel.readString(),
              parcel.readString(),
              parcel.readString(),
              parcel.readString(),
              parcel.readString(),
              parcel.readString(),
              parcel.readInt() == 1,
              parcel.readLong()
      );
    }

    @Override
    public Product[] newArray(int size) {
      return new Product[size];
    }
  };

  private Product(String vendorId, String sku, String price, String currency, String name, String description, boolean isSubscription, long microsPrice) {
    this.vendorId = vendorId;
    this.sku = sku;
    this.price = price;
    this.currency = currency;
    this.name = name;
    this.description = description;
    this.isSubscription = isSubscription;
    this.microsPrice = microsPrice;
  }

  public String vendorId() {
    return vendorId;
  }

  public String sku() {
    return sku;
  }

  public String price() {
    return price;
  }

  public String currency() {
    return currency;
  }

  public String name() {
    return name;
  }

  public String description() {
    return description;
  }

  public boolean isSubscription() {
    return isSubscription;
  }

  public long microsPrice() {
    return microsPrice;
  }

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
    return new Product(
            vendorId,
            sku,
            price,
            currency,
            name,
            description,
            isSubscription,
            microsPrice) {
    };
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

  @Override
  public int describeContents() {
      return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
      parcel.writeString(vendorId);
      parcel.writeString(sku);
      parcel.writeString(price);
      parcel.writeString(currency);
      parcel.writeString(name);
      parcel.writeString(description);
      parcel.writeInt(isSubscription ? 1 : 0);
      parcel.writeLong(microsPrice);
  }
}
