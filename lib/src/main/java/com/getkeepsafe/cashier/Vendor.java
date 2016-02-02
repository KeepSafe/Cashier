package com.getkeepsafe.cashier;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.getkeepsafe.cashier.logging.Logger;

public interface Vendor {
    /** Cannot buy the product, either do to no network connectivity or service being unavailable */
    int PURCHASE_UNAVAILABLE = 0;

    /** User canceled the purchase */
    int PURCHASE_CANCEL = 1;

    /** Unknown error **/
    int PURCHASE_FAILURE = 2;

    /** User already owns the product */
    int PURCHASE_ALREADY_OWNED = 3;

    /** User does not own the product */
    int PURCHASE_NOT_OWNED = 4;

    /** Purchase seems to be successful, however the expected result is malformed */
    int PURCHASE_SUCCESS_RESULT_MALFORMED = 5;

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
    void setLogger(@Nullable Logger logger);

    boolean available();
    boolean canPurchase(@NonNull Product product);
    boolean onActivityResult(final int requestCode, final int resultCode, final Intent data);
}
