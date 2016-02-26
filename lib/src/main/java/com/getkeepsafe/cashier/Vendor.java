package com.getkeepsafe.cashier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    @NonNull
    String id();
    void initialize(@NonNull Context context, @NonNull InitializationListener listener);
    void dispose(@NonNull Context context);
    void purchase(@NonNull Activity activity,
                  @NonNull Product product,
                  @Nullable String developerPayload,
                  @NonNull PurchaseListener listener);

    void consume(@NonNull Activity activity,
                 @NonNull Purchase purchase,
                 @NonNull ConsumeListener listener);

    void getInventory(@NonNull Context context,
                      @Nullable List<String> itemSkus,
                      @Nullable List<String> subSkus,
                      @NonNull InventoryListener listener);

    void setLogger(@Nullable Logger logger);

    boolean available();
    boolean canPurchase(@NonNull Product product);
    boolean onActivityResult(int requestCode, int resultCode, Intent data);

    Product getProductFrom(@NonNull JSONObject json) throws JSONException;
    Purchase getPurchaseFrom(@NonNull JSONObject json) throws JSONException;
}
