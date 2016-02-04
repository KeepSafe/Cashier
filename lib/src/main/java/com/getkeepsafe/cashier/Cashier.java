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

import org.json.JSONException;
import org.json.JSONObject;

public class Cashier {
    private static VendorFactory vendorFactory;
    private static VendorFactory debugVendorFactory;
    private static boolean debug;

    private final Activity activity;
    private final Vendor vendor;

    static {
        debug = false;
        vendorFactory = new VendorFactory() {
            @Override
            public Vendor get(@NonNull final String id) throws VendorMissingException {
                Check.notNull(id, "Vendor ID");
                if (debug) {
                    return debugVendorFactory.get(id);
                }

                if (id.equals(GooglePlayConstants.VENDOR_PACKAGE)) {
                    return new InAppBillingV3Vendor(new ProductionInAppBillingV3API());
                }

                throw new VendorMissingException(id);
            }
        };

        debugVendorFactory = new VendorFactory() {
            @Override
            public Vendor get(@NonNull String id) throws VendorMissingException {
                if (id.equals(GooglePlayConstants.VENDOR_PACKAGE)) {
                    // TODO: TestInAppBillingV3API
                    return new InAppBillingV3Vendor(new ProductionInAppBillingV3API());
                }

                throw new VendorMissingException(id);
            }
        };
    }

    public void setDebugMode(final boolean status) {
        debug = status;
    }

    public static void setVendorFactory(@NonNull final VendorFactory factory) {
        vendorFactory = factory;
    }

    public static void setDebugVendorFactory(@NonNull final VendorFactory factory) {
        debugVendorFactory = factory;
    }

    public static Builder forGooglePlay(@NonNull final Activity activity) {
        Check.notNull(activity, "Activity");
        return new Builder(activity).forVendor(
                new InAppBillingV3Vendor(new ProductionInAppBillingV3API()));
    }

    public static Builder forAppInstaller(@NonNull final Activity activity)
            throws VendorFactory.VendorMissingException {
        Check.notNull(activity, "Activity");
        final String installer = activity
                .getPackageManager()
                .getInstallerPackageName(activity.getPackageName());
        return new Builder(activity).forVendor(vendorFactory.get(installer));
    }

    public static Builder forProduct(@NonNull final Activity activity,
                                     @NonNull final Product product)
            throws VendorFactory.VendorMissingException {
        Check.notNull(product, "Product");
        return new Builder(activity).forVendor(vendorFactory.get(product.vendorId));
    }

    public static Product productFromJson(@NonNull final String json)
            throws JSONException, VendorFactory.VendorMissingException {
        return productFromJson(new JSONObject(json));
    }

    public static Product productFromJson(@NonNull final JSONObject json)
            throws JSONException, VendorFactory.VendorMissingException {
        final String vendorId = json.getString(Product.KEY_VENDOR_ID);
        final Vendor vendor = vendorFactory.get(vendorId);
        return vendor.getProductFrom(json);
    }

    public static Purchase purchaseFromJson(@NonNull final String json)
            throws JSONException, VendorFactory.VendorMissingException {
        return purchaseFromJson(new JSONObject(json));
    }

    public static Purchase purchaseFromJson(@NonNull final JSONObject json)
            throws JSONException, VendorFactory.VendorMissingException {
        final String vendorId = json.getString(Product.KEY_VENDOR_ID);
        final Vendor vendor = vendorFactory.get(vendorId);
        return vendor.getPurchaseFrom(json);
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
        purchase(product, null, listener);
    }

    public void purchase(@NonNull final Product product,
                         @Nullable final String developerPayload,
                         @NonNull final PurchaseListener listener) {
        Check.notNull(product, "Product");
        Check.notNull(listener, "Listener");
        vendor.initialize(activity, new Vendor.InitializationListener() {
            @Override
            public void initialized() {
                if (!vendor.available() || !vendor.canPurchase(product)) {
                    listener.failure(product, new Vendor.Error(Vendor.PURCHASE_UNAVAILABLE, -1));
                    return;
                }

                vendor.purchase(activity, product, developerPayload, listener);
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
}
