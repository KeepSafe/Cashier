package com.getkeepsafe.cashier;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.getkeepsafe.cashier.utilities.Check;

public class Cashier {
    private final Activity activity;
    private final Vendor vendor;

    public Cashier(@NonNull final Activity activity,
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
                    listener.failure(product, Vendor.PURCHASE_UNAVAILABLE);
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

    public void dispose() {
        vendor.dispose(activity);
    }
}
