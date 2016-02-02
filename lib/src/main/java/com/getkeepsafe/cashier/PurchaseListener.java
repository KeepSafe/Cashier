package com.getkeepsafe.cashier;

import android.support.annotation.NonNull;

public interface PurchaseListener {
    void success(@NonNull Product product, @NonNull Receipt receipt);
    void failure(@NonNull Product product, final int code);
}
