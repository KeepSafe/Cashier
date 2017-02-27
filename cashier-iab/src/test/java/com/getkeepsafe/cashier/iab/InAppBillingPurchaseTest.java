package com.getkeepsafe.cashier.iab;

import com.getkeepsafe.LibraryProjectRobolectricTestRunner;
import com.getkeepsafe.cashier.Product;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LibraryProjectRobolectricTestRunner.class)
public class InAppBillingPurchaseTest {
    @Test
    public void suppliesTestOrderIdWhenMissing() throws JSONException {
        Product testProduct = Product.create(
                "{\"micros-price\":1," +
                "\"vendor-id\":\"1\"," +
                "\"price\":\"1\"," +
                "\"name\":\"1\"," +
                "\"description\":\"1\"," +
                "\"currency\":\"1\"," +
                "\"subscription\":false," +
                "\"sku\":\"so.product.much.purchase\"}"
        );
        String purchaseData = "{\"autoRenewing\":false,\"packageName\":\"com.getkeepsafe.cashier.sample\",\"productId\":\"so.product.much.purchase\",\"purchaseTime\":1476077957823,\"purchaseState\":0,\"developerPayload\":\"hello-cashier!\",\"purchaseToken\":\"15d12f9b-82fc-4977-b49c-aef730a10463\"}";
        InAppBillingPurchase purchase = InAppBillingPurchase.create(testProduct, purchaseData, "test");
        assertThat(purchase.orderId()).isEqualTo(InAppBillingPurchase.GP_ORDER_ID_TEST);
    }

    @Test
    public void doesNotSupplyTestOrderIdByDefault() throws JSONException {
        Product testProduct = Product.create(
                "{\"micros-price\":1," +
                "\"vendor-id\":\"1\"," +
                "\"price\":\"1\"," +
                "\"name\":\"1\"," +
                "\"description\":\"1\"," +
                "\"currency\":\"1\"," +
                "\"subscription\":false," +
                "\"sku\":\"so.product.much.purchase\"}"
        );
        String purchaseData = "{\"orderId\":\"testtest\",\"autoRenewing\":false,\"packageName\":\"com.getkeepsafe.cashier.sample\",\"productId\":\"so.product.much.purchase\",\"purchaseTime\":1476077957823,\"purchaseState\":0,\"developerPayload\":\"hello-cashier!\",\"purchaseToken\":\"15d12f9b-82fc-4977-b49c-aef730a10463\"}";
        InAppBillingPurchase purchase = InAppBillingPurchase.create(testProduct, purchaseData, "test");
        assertThat(purchase.orderId()).isNotEqualTo(InAppBillingPurchase.GP_ORDER_ID_TEST);
    }
}
