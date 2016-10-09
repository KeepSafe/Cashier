package com.getkeepsafe.cashier;

public interface InventoryListener {
    void success(Inventory inventory);
    void failure(Vendor.Error error);
}
