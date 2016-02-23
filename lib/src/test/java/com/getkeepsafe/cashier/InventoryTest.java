package com.getkeepsafe.cashier;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;

public class InventoryTest {
    @Test
    public void addsPurchases() {
        final Purchase purchase = ValueFactory.aPurchase();
        final Inventory inventory = new Inventory();
        inventory.addPurchase(purchase);
        assertThat(inventory.purchases().size(), is(1));
        assertThat(inventory.purchases().get(0), is(purchase));

        final List<Purchase> purchases = new ArrayList<>(2);
        purchases.add(ValueFactory.aPurchase());
        purchases.add(ValueFactory.aPurchase());
        purchases.add(ValueFactory.aPurchase());
        inventory.addPurchases(purchases);

        final List<Purchase> purchaseList = inventory.purchases();
        assertThat(purchaseList.size(), is(4));
        for (int i = 1; i < purchaseList.size(); i++) {
            assertThat(purchaseList.get(i), is(purchases.get(i - 1)));
        }
    }

    @Test
    public void addsProducts() {
        final Product product = ValueFactory.aProduct();
        final Inventory inventory = new Inventory();
        inventory.addProduct(product);
        assertThat(inventory.products().size(), is(1));
        assertThat(inventory.products().get(0), is(product));

        final List<Product> products = new ArrayList<>(2);
        products.add(ValueFactory.aProduct());
        products.add(ValueFactory.aProduct());
        products.add(ValueFactory.aProduct());
        inventory.addProducts(products);

        final List<Product> productList = inventory.products();
        assertThat(productList.size(), is(4));
        for (int i = 1; i < productList.size(); i++) {
            assertThat(productList.get(i), is(products.get(i - 1)));
        }
    }

    @Test
    public void returnsImmutableLists() {
        final Inventory inventory = new Inventory();
        final List<Product> products = inventory.products();
        assertThat(products.add(ValueFactory.aProduct()), thrown());
    }
}
