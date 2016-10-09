package com.getkeepsafe.cashier;

public class VendorMissingException extends RuntimeException {
    public final String vendorId;

    public VendorMissingException(String vendorId) {
        this(vendorId, null);
    }

    public VendorMissingException(String vendorId, String message) {
        super(message);
        this.vendorId = vendorId;
    }
}
