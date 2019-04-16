package com.getkeepsafe.cashier.sample.googleplaybilling;

import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;

public class Item {

    public Product product;

    public Purchase purchase;

    public String title;

    public String price;

    public boolean isSubscription;

    public boolean isPurchased;

    public Item() {
    }

    public Item(String title, String price, boolean isSubscription, boolean isPurchased) {
        this.title = title;
        this.price = price;
        this.isSubscription = isSubscription;
        this.isPurchased = isPurchased;
    }
}
