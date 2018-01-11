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

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.getkeepsafe.cashier.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

public interface Vendor {
  interface InitializationListener {
    void initialized();

    void unavailable();
  }

  class Error {
    public final int code;
    public final int vendorCode;

    public Error(int code, int vendorCode) {
      this.code = code;
      this.vendorCode = vendorCode;
    }

    @Override
    public boolean equals(Object other) {
      if (other == null) return false;
      if (!(other instanceof Error)) return false;
      final Error o = (Error) other;
      return this.code == o.code && this.vendorCode == o.vendorCode;
    }
  }

  String id();

  void initialize(Context context, InitializationListener listener);

  void dispose(Context context);

  void purchase(Fragment fragment,
                Product product,
                String developerPayload,
                PurchaseListener listener);

  void consume(Context context,
               Purchase purchase,
               ConsumeListener listener);

  void getInventory(Context context,
                    Collection<String> itemSkus,
                    Collection<String> subSkus,
                    InventoryListener listener);

  void getProductDetails(Context context, String sku, boolean isSubscription, ProductDetailsListener listener);

  void setLogger(Logger logger);

  boolean available();

  boolean canPurchase(Product product);

  boolean onActivityResult(int requestCode, int resultCode, Intent data);

  Product getProductFrom(JSONObject json) throws JSONException;

  Purchase getPurchaseFrom(JSONObject json) throws JSONException;
}
