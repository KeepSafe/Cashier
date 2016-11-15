package com.getkeepsafe.cashier;

public interface ProductDetailsListener {
    void success(Product product);
    void failure(Vendor.Error error);
}
