package com.getkeepsafe.cashier;

import android.support.annotation.NonNull;

import com.getkeepsafe.cashier.utilities.Check;

public class Product {
    public final String sku;
    public final boolean isSubscription;

    public static Product item(@NonNull final String sku) {
        return new Product(sku, false);
    }

    public static Product subscription(@NonNull final String sku) {
        return new Product(sku, true);
    }

    public Product(@NonNull final String sku, final boolean isSubscription) {
        this.sku = Check.notNull(sku, "SKU");
        this.isSubscription = isSubscription;
    }
}
