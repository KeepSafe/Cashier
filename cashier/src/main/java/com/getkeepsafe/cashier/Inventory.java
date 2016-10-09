package com.getkeepsafe.cashier;

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

    public void addPurchase(Purchase purchase) {
        purchases.add(purchase);
    }

    public void addPurchases(Collection<? extends Purchase> purchases) {
        this.purchases.addAll(purchases);
    }

    public List<Product> products() {
        return Collections.unmodifiableList(products);
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public void addProducts(Collection<? extends Product> products) {
        this.products.addAll(products);
    }
}
