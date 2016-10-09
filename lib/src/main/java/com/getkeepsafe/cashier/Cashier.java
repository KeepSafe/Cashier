package com.getkeepsafe.cashier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;



import com.getkeepsafe.cashier.googleplay.FakeInAppBillingV3Api;
import com.getkeepsafe.cashier.googleplay.GooglePlayConstants;
import com.getkeepsafe.cashier.googleplay.InAppBillingV3Vendor;
import com.getkeepsafe.cashier.googleplay.ProductionInAppBillingV3API;
import com.getkeepsafe.cashier.logging.Logger;
import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Cashier {
    private static VendorFactory vendorFactory = new VendorFactory() {
        @Override
        public Vendor get(final String id) throws VendorFactory.VendorMissingException
        {
            Check.notNull(id, "Vendor ID");

            if (id.equals(GooglePlayConstants.VENDOR_PACKAGE)) {
                return new InAppBillingV3Vendor(new ProductionInAppBillingV3API());
            }

            throw new VendorFactory.VendorMissingException(id);
        }
    };

    private final Context context;
    private final Vendor vendor;

    public static void setVendorFactory(final VendorFactory factory) {
        vendorFactory = factory;
    }

    public static Builder forGooglePlay(final Context context) {
        Check.notNull(context, "Context");
        return new Builder(context)
                .forVendor(new InAppBillingV3Vendor(new ProductionInAppBillingV3API()));
    }

    public static Builder forDebugGooglePlay(final Context context) {
        return new Builder(context)
                .forVendor(new InAppBillingV3Vendor(new FakeInAppBillingV3Api(context)));
    }

    public static Builder forAppInstaller(final Context context)
            throws VendorFactory.VendorMissingException {
        Check.notNull(context, "Context");
        final String installer = context
                .getPackageManager()
                .getInstallerPackageName(context.getPackageName());
        return new Builder(context).forVendor(vendorFactory.get(installer));
    }

    public static Builder forProduct(final Context context,
                                     final Product product)
            throws VendorFactory.VendorMissingException {
        Check.notNull(context, "Context");
        Check.notNull(product, "Product");
        return new Builder(context).forVendor(vendorFactory.get(product.vendorId));
    }

    public static Product productFromJson(final String json)
            throws JSONException, VendorFactory.VendorMissingException {
        return productFromJson(new JSONObject(json));
    }

    public static Product productFromJson(final JSONObject json)
            throws JSONException, VendorFactory.VendorMissingException {
        final String vendorId = json.getString(Product.KEY_VENDOR_ID);
        final Vendor vendor = vendorFactory.get(vendorId);
        return vendor.getProductFrom(json);
    }

    public static Purchase purchaseFromJson(final String json)
            throws JSONException, VendorFactory.VendorMissingException {
        return purchaseFromJson(new JSONObject(json));
    }

    public static Purchase purchaseFromJson(final JSONObject json)
            throws JSONException, VendorFactory.VendorMissingException {
        final String vendorId = json.getString(Product.KEY_VENDOR_ID);
        final Vendor vendor = vendorFactory.get(vendorId);
        return vendor.getPurchaseFrom(json);
    }

    private Cashier(final Context context,
                    final Vendor vendor) {
        this.context = Check.notNull(context, "Context");
        this.vendor = Check.notNull(vendor, "Vendor");
    }

    public void purchase(final Product product, final PurchaseListener listener) {
        purchase(product, null, listener);
    }

    public void purchase(final Product product,
                         final String developerPayload,
                         final PurchaseListener listener) {
        Check.notNull(product, "Product");
        Check.notNull(listener, "Listener");
        final Activity activity = (Activity) context;
        vendor.initialize(context, new Vendor.InitializationListener() {
            @Override
            public void initialized() {
                if (!vendor.available() || !vendor.canPurchase(product)) {
                    listener.failure(product, new Vendor.Error(Vendor.PURCHASE_UNAVAILABLE, -1));
                    return;
                }

                final String payload = developerPayload == null ? "" : developerPayload;
                vendor.purchase(activity, product, payload, listener);
            }

            @Override
            public void unavailable() {
                listener.failure(product,
                        new Vendor.Error(VendorConstants.PURCHASE_UNAVAILABLE, -1));
            }
        });
    }

    public void consume(final Purchase purchase, final ConsumeListener listener) {
        Check.notNull(purchase, "Purchase");
        Check.notNull(listener, "Listener");
        if (purchase.isSubscription) {
            throw new IllegalArgumentException("Cannot consume a subscription type!");
        }
        final Activity activity = (Activity) context;
        vendor.initialize(context, new Vendor.InitializationListener() {
            @Override
            public void initialized() {
                vendor.consume(activity, purchase, listener);
            }

            @Override
            public void unavailable() {
                listener.failure(purchase,
                        new Vendor.Error(VendorConstants.CONSUME_UNAVAILABLE, -1));
            }
        });
    }

    public void getInventory(final InventoryListener listener) {
        getInventory(null, null, listener);
    }

    public void getInventory(final List<String> itemSkus,
                             final List<String> subSkus,
                             final InventoryListener listener) {
        Check.notNull(listener, "Listener");

        vendor.initialize(context, new Vendor.InitializationListener() {
            @Override
            public void initialized() {
                vendor.getInventory(context, itemSkus, subSkus, listener);
            }

            @Override
            public void unavailable() {
                listener.failure(new Vendor.Error(VendorConstants.INVENTORY_QUERY_UNAVAILABLE, -1));
            }
        });
    }

    public String vendorId() {
        return vendor.id();
    }

    public void dispose() {
        vendor.dispose(context);
    }

    public boolean onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        return vendor.onActivityResult(requestCode, resultCode, data);
    }

    public static class Builder {
        private final Context context;
        private Vendor vendor;
        private Logger logger;

        public Builder(final Context context) {
            this.context = Check.notNull(context, "Context");
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

            return new Cashier(context, vendor);
        }
    }
}
