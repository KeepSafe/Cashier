package com.getkeepsafe.cashier;

import android.support.annotation.NonNull;

import com.getkeepsafe.cashier.utilities.Check;

public class Purchase extends Product {
    public final String token;

    public Purchase(@NonNull final Product product,
                     @NonNull final String token) {
        super(product.sku, product.isSubscription);
        this.token = Check.notNull(token, "Token");
    }
}
