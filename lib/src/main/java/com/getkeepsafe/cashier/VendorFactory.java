package com.getkeepsafe.cashier;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.getkeepsafe.cashier.utilities.Check;

public interface VendorFactory {
    Vendor get(@NonNull String id) throws VendorMissingException;

    class VendorMissingException extends Exception {
        public final String vendorId;

        public VendorMissingException(@NonNull final String vendorId) {
            this(vendorId, null);
        }

        public VendorMissingException(@NonNull final String vendorId,
                                      @Nullable final String message) {
            super(message);
            this.vendorId = Check.notNull(vendorId, "Vendor ID");
        }
    }
}
