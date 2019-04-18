package com.getkeepsafe.cashier.billing;

import com.android.billingclient.api.Purchase;
import com.getkeepsafe.cashier.Product;

import org.json.JSONException;

public class TestPurchase extends Purchase {

    private static String TEST_JSON = "{ \""
            +GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE+"\": "
            +GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE_PURCHASED+" }";

    private Product product;

    public TestPurchase(Product product) throws JSONException {
        super(TEST_JSON, GooglePlayBillingSecurity.sign(TestData.TEST_PRIVATE_KEY, TEST_JSON));
        this.product = product;
    }

    public TestPurchase(Product product, String signature) throws JSONException {
        super(TEST_JSON, signature);
        this.product = product;
    }

    public TestPurchase(Product product, String json, String signature) throws JSONException {
        super(json, signature);
        this.product = product;
    }

    @Override
    public String getSku() {
        return product.sku();
    }

    @Override
    public String getOrderId() {
        return "test-order-id";
    }

    @Override
    public String getPurchaseToken() {
        return "test-purchase-token";
    }
}
