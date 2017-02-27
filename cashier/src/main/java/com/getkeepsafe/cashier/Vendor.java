package com.getkeepsafe.cashier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;



import com.getkeepsafe.cashier.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

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
    void purchase(Activity activity,
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
