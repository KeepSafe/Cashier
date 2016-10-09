package com.getkeepsafe.cashier;



public interface PurchaseListener {
    void success(Purchase purchase);
    void failure(Product product, Vendor.Error error);
}
