package com.getkeepsafe.cashier;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InventoryTest {
    @Test
    public void addsPurchases() {
        final CashierPurchase purchase = ValueFactory.aPurchase();
        final Inventory inventory = new Inventory();
        inventory.addPurchase(purchase);
        assertThat(inventory.purchases().size()).isEqualTo(1);
        assertThat(inventory.purchases().get(0)).isEqualTo(purchase);

        final List<CashierPurchase> purchases = new ArrayList<>(3);
        purchases.add(ValueFactory.aPurchase());
        purchases.add(ValueFactory.aPurchase());
        purchases.add(ValueFactory.aPurchase());
        inventory.addPurchases(purchases);

        final List<Purchase> purchaseList = inventory.purchases();
        assertThat(purchaseList.size()).isEqualTo(4);
        for (int i = 1; i < purchaseList.size(); i++) {
            assertThat(purchaseList.get(i)).isEqualTo(purchases.get(i - 1));
        }
    }

    @Test
    public void addsProducts() {
        final Product product = ValueFactory.aProduct();
        final Inventory inventory = new Inventory();
        inventory.addProduct(product);
        assertThat(inventory.products().size()).isEqualTo(1);
        assertThat(inventory.products().get(0)).isEqualTo(product);

        final List<Product> products = new ArrayList<>(2);
        products.add(ValueFactory.aProduct());
        products.add(ValueFactory.aProduct());
        products.add(ValueFactory.aProduct());
        inventory.addProducts(products);

        final List<Product> productList = inventory.products();
        assertThat(productList.size()).isEqualTo(4);
        for (int i = 1; i < productList.size(); i++) {
            assertThat(productList.get(i)).isEqualTo(products.get(i - 1));
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void returnsImmutableProductList() {
        final Inventory inventory = new Inventory();
        final List<Product> products = inventory.products();
        products.add(ValueFactory.aProduct());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void returnsImmutablePurchaseList() {
        final Inventory inventory = new Inventory();
        final List<Purchase> purchases = inventory.purchases();
        purchases.add(ValueFactory.aPurchase());
    }
}
