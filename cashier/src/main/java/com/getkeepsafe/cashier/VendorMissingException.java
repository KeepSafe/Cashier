package com.getkeepsafe.cashier;

import android.support.annotation.Nullable;

public class VendorMissingException extends RuntimeException {
    public final String vendorId;

    public VendorMissingException(String vendorId) {
        this(vendorId, null);
    }

    public VendorMissingException(String vendorId, @Nullable String message) {
        super(message);
        this.vendorId = vendorId;
    }
}
