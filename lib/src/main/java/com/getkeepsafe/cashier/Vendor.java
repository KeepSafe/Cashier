package com.getkeepsafe.cashier;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.getkeepsafe.cashier.logging.Logger;

public interface Vendor extends VendorConstants {
    interface InitializationListener {
        void initialized();
    }

    void initialize(@NonNull Activity activity, @NonNull InitializationListener listener);
    void dispose(@NonNull Activity activity);
    void purchase(@NonNull Activity activity,
                  @NonNull Product product,
                  @NonNull PurchaseListener listener);
    void consume(@NonNull Activity activity,
                 @NonNull Purchase purchase,
                 @NonNull ConsumeListener listener);
    void getInventory(@NonNull Activity activity, @NonNull InventoryListener listener);
    void setLogger(@Nullable Logger logger);

    boolean available();
    boolean canPurchase(@NonNull Product product);
    boolean onActivityResult(final int requestCode, final int resultCode, final Intent data);
}
