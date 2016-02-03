package com.getkeepsafe.cashier;

import android.support.annotation.NonNull;

import com.getkeepsafe.cashier.utilities.Check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Inventory {
    private final List<Purchase> purchases;
    private final List<Product> products;

    public Inventory() {
        purchases = new ArrayList<>();
        products = new ArrayList<>();
    }

    public List<Purchase> purchases() {
        return Collections.unmodifiableList(purchases);
    }

    public void addPurchase(@NonNull final Purchase purchase) {
        purchases.add(Check.notNull(purchase, "Purchase"));
    }

    public void addPurchases(@NonNull final Collection<? extends Purchase> purchases) {
        this.purchases.addAll(Check.notNull(purchases, "Purchases"));
    }

    public List<Product> products() {
        return Collections.unmodifiableList(products);
    }

    public void addProduct(@NonNull final Product product) {
        products.add(Check.notNull(product, "Product"));
    }

    public void addProducts(@NonNull final Collection<? extends Product> products) {
        this.products.addAll(Check.notNull(products, "Products"));
    }
}
