package com.getkeepsafe.cashier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;



import com.getkeepsafe.cashier.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public interface Vendor extends VendorConstants {
    interface InitializationListener {
        void initialized();
        void unavailable();
    }

    class Error {
        public final int code;
        public final int vendorCode;

        public Error(final int code, final int vendorCode) {
            this.code = code;
            this.vendorCode = vendorCode;
        }
    }

    String id();
    void initialize(Context context, InitializationListener listener);
    void dispose(Context context);
    void purchase(Activity activity,
                  Product product,
                  String developerPayload,
                  PurchaseListener listener);

    void consume(Activity activity,
                 Purchase purchase,
                 ConsumeListener listener);

    void getInventory(Context context,
                      List<String> itemSkus,
                      List<String> subSkus,
                      InventoryListener listener);

    void setLogger(Logger logger);

    boolean available();
    boolean canPurchase(Product product);
    boolean onActivityResult(int requestCode, int resultCode, Intent data);

    Product getProductFrom(JSONObject json) throws JSONException;
    Purchase getPurchaseFrom(JSONObject json) throws JSONException;
}
