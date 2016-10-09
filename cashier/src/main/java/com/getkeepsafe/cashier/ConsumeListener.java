package com.getkeepsafe.cashier;

public interface ConsumeListener {
    void success(Purchase purchase);
    void failure(Purchase purchase, Vendor.Error error);
}
