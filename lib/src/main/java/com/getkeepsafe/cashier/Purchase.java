package com.getkeepsafe.cashier;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.getkeepsafe.cashier.utilities.Check;

public class Purchase extends Product {
    public final String orderId;
    public final String token;

    private String extras;

    public Purchase(@NonNull final Product product,
                    @NonNull final String orderId,
                    @NonNull final String token) {
        super(product.sku, product.isSubscription);
        this.orderId = Check.notNull(orderId, "Order ID");
        this.token = Check.notNull(token, "Token");
    }

    @Nullable
    public String getExtras() {
         return extras;
    }

    public void setExtras(final String extras) {
        this.extras = extras;
    }
}
