package com.getkeepsafe.cashier;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.getkeepsafe.cashier.googleplay.GooglePlayConstants;
import com.getkeepsafe.cashier.googleplay.InAppBillingV3Vendor;
import com.getkeepsafe.cashier.googleplay.ProductionInAppBillingV3API;
import com.getkeepsafe.cashier.logging.Logger;
import com.getkeepsafe.cashier.utilities.Check;

public class Cashier {
    private final Activity activity;
    private final Vendor vendor;

    public static Builder forGooglePlay(@NonNull final Activity activity) {
        Check.notNull(activity, "Activity");
        return forGooglePlay(activity, null);
    }

    public static Builder forGooglePlay(@NonNull final Activity activity,
                                        @Nullable final String developerPayload) {
        Check.notNull(activity, "Activity");
        return new Builder(activity).forVendor(
                new InAppBillingV3Vendor(
                        new ProductionInAppBillingV3API(activity.getPackageName()),
                        developerPayload));
    }

    public static Builder forAppInstaller(@NonNull final Activity activity)
            throws VendorMissingException {
        return forAppInstaller(activity, null);
    }

    public static Builder forAppInstaller(@NonNull final Activity activity,
                                          @Nullable final String developerPayload)
            throws VendorMissingException {
        Check.notNull(activity, "Activity");
        final String installer = activity
                .getPackageManager()
                .getInstallerPackageName(activity.getPackageName());
        return new Builder(activity)
                .forVendor(idToVendor(installer, activity, developerPayload));
    }

    public static Builder forPurchase(@NonNull final Activity activity,
                                      @NonNull final Purchase purchase)
            throws VendorMissingException {
        return forPurchase(activity, purchase, null);
    }

    public static Builder forPurchase(@NonNull final Activity activity,
                                      @NonNull final Purchase purchase,
                                      @Nullable final String developerPayload)
            throws VendorMissingException {
        Check.notNull(purchase, "Purchase");
        return new Builder(activity)
                .forVendor(idToVendor(purchase.vendorId, activity, developerPayload));
    }

    private static Vendor idToVendor(@NonNull final String id,
                              @NonNull final Activity activity,
                              @Nullable final String developerPayload)
            throws VendorMissingException {
        if (id.equals(GooglePlayConstants.VENDOR_PACKAGE)) {
            return new InAppBillingV3Vendor(
                    new ProductionInAppBillingV3API(activity.getPackageName()), developerPayload);
        }

        throw new VendorMissingException(id);
    }

    // TODO: Flesh out
//    public static Builder forDebugGooglePlay(@NonNull final Activity activity) {
//        return forDebugGooglePlay(activity, null);
//    }
//
//    public static Builder forDebugGooglePlay(@NonNull final Activity activity,
//                                             @Nullable final String developerPayload) {
//        return new Builder(activity).forVendor(
//                new InAppBillingV3Vendor())
//    }

    private Cashier(@NonNull final Activity activity,
                   @NonNull final Vendor vendor) {
        this.activity = Check.notNull(activity, "Activity");
        this.vendor = Check.notNull(vendor, "Vendor");
    }

    public void purchase(@NonNull final Product product, @NonNull final PurchaseListener listener) {
        Check.notNull(product, "Product");
        Check.notNull(listener, "Listener");
        vendor.initialize(activity, new Vendor.InitializationListener() {
            @Override
            public void initialized() {
                if (!vendor.available() || !vendor.canPurchase(product)) {
                    listener.failure(product, new Vendor.Error(Vendor.PURCHASE_UNAVAILABLE, -1));
                    return;
                }

                vendor.purchase(activity, product, listener);
            }
        });
    }

    public void consume(@NonNull final Purchase purchase, @NonNull final ConsumeListener listener) {
        Check.notNull(purchase, "Purchase");
        Check.notNull(listener, "Listener");
        if (purchase.isSubscription) {
            throw new IllegalArgumentException("Cannot consume a subscription type!");
        }

        vendor.initialize(activity, new Vendor.InitializationListener() {
            @Override
            public void initialized() {
                vendor.consume(activity, purchase, listener);
            }
        });
    }

    public void getInventory(@NonNull final InventoryListener listener) {
        Check.notNull(listener, "Listener");

        vendor.initialize(activity, new Vendor.InitializationListener() {
            @Override
            public void initialized() {
                vendor.getInventory(activity, listener);
            }
        });
    }

    public void dispose() {
        vendor.dispose(activity);
    }

    public boolean onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        return vendor.onActivityResult(requestCode, resultCode, data);
    }

    public static class Builder {
        private final Activity activity;
        private Vendor vendor;
        private Logger logger;

        public Builder(final Activity activity) {
            this.activity = Check.notNull(activity, "Activity");
        }

        public Builder forVendor(final Vendor vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder withLogger(final Logger logger) {
            this.logger = logger;
            return this;
        }

        public Cashier build() {
            if (logger != null) {
                vendor.setLogger(logger);
            }

            return new Cashier(activity, vendor);
        }
    }

    public static class VendorMissingException extends Exception {
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
