package com.getkeepsafe.cashier;

import android.support.annotation.NonNull;

public interface InventoryListener {
    void success(@NonNull Inventory inventory);
    void failure(@NonNull final Vendor.Error error);
}
