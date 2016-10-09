package com.getkeepsafe.cashier;




import com.getkeepsafe.cashier.utilities.Check;

public interface VendorFactory {
    Vendor get(String id) throws VendorMissingException;

    class VendorMissingException extends Exception {
        public final String vendorId;

        public VendorMissingException(final String vendorId) {
            this(vendorId, null);
        }

        public VendorMissingException(final String vendorId,
                                      final String message) {
            super(message);
            this.vendorId = Check.notNull(vendorId, "Vendor ID");
        }
    }
}
