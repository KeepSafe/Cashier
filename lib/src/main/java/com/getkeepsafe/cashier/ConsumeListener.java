package com.getkeepsafe.cashier;

import android.support.annotation.NonNull;

public interface ConsumeListener {
    void success(@NonNull Purchase purchase);
    void failure(@NonNull Purchase purchase, @NonNull Vendor.Error error);
}
