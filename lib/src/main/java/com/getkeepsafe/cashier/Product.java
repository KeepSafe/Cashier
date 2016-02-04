package com.getkeepsafe.cashier;

import android.support.annotation.NonNull;

import com.getkeepsafe.cashier.utilities.Check;

public class Product {
    public final String sku;
    public final String price;
    public final String currency;
    public final String name;
    public final String description;
    public final boolean isSubscription;
    public final long microsPrice;

    public static Product item(@NonNull final String sku,
                               @NonNull final String price,
                               @NonNull final String currency,
                               @NonNull final String name,
                               @NonNull final String description,
                               final long microsPrice) {
        return new Product(sku, price, currency, name, description, false, microsPrice);
    }

    public static Product subscription(@NonNull final String sku,
                                       @NonNull final String price,
                                       @NonNull final String currency,
                                       @NonNull final String name,
                                       @NonNull final String description,
                                       final long microsPrice) {
        return new Product(sku, price, currency, name, description, true, microsPrice);
    }

    public Product(@NonNull final String sku,
                   @NonNull final String price,
                   @NonNull final String currency,
                   @NonNull final String name,
                   @NonNull final String description,
                   final boolean isSubscription,
                   final long microsPrice) {
        this.sku = Check.notNull(sku, "SKU");
        this.price = Check.notNull(price, "Price");
        this.currency = Check.notNull(currency, "Currency");
        this.name = Check.notNull(name, "Product Name");
        this.description = Check.notNull(description, "Product Description");
        this.isSubscription = isSubscription;
        this.microsPrice = microsPrice;
    }

    public Product(@NonNull final Product product) {
        this(product.sku,
                product.price,
                product.currency,
                product.name,
                product.description,
                product.isSubscription,
                product.microsPrice);
    }
}
