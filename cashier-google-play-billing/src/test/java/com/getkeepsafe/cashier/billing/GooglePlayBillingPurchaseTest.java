package com.getkeepsafe.cashier.billing;

import com.android.billingclient.api.Purchase;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class GooglePlayBillingPurchaseTest {

    private static final String JSON_PURCHASED = "{ \""
            +GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE+"\": "
            +GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE_PURCHASED+" }";

    private static final String JSON_CANCELED = "{ \""
            +GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE+"\": "
            +GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE_CANCELED+" }";

    private static final String JSON_REFUNDED = "{ \""
            +GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE+"\": "
            +GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE_REFUNDED+" }";

    private static final String SIGNATURE = "1234567890";

    @Test
    public void create_purchased() throws JSONException  {
        Purchase billingPurchase = new TestPurchase(TestData.productInappA, JSON_PURCHASED, SIGNATURE);
        GooglePlayBillingPurchase purchase = GooglePlayBillingPurchase.create(TestData.productInappA, billingPurchase);

        assertEquals(billingPurchase.getOrderId(), purchase.orderId());
        assertEquals(billingPurchase.getPurchaseToken(), purchase.token());
        assertEquals(billingPurchase.getSku(), purchase.product().sku());
        assertEquals(JSON_PURCHASED, purchase.receipt());
        assertTrue(purchase.purchased());
    }

    @Test
    public void create_canceled() throws JSONException  {
        Purchase billingPurchase = new TestPurchase(TestData.productInappA, JSON_CANCELED, SIGNATURE);
        GooglePlayBillingPurchase purchase = GooglePlayBillingPurchase.create(TestData.productInappA, billingPurchase);

        assertEquals(billingPurchase.getOrderId(), purchase.orderId());
        assertEquals(billingPurchase.getPurchaseToken(), purchase.token());
        assertEquals(billingPurchase.getSku(), purchase.product().sku());
        assertEquals(JSON_CANCELED, purchase.receipt());
        assertTrue(purchase.canceled());
    }

    @Test
    public void create_refunded() throws JSONException  {
        Purchase billingPurchase = new TestPurchase(TestData.productInappA, JSON_REFUNDED, SIGNATURE);
        GooglePlayBillingPurchase purchase = GooglePlayBillingPurchase.create(TestData.productInappA, billingPurchase);

        assertEquals(billingPurchase.getOrderId(), purchase.orderId());
        assertEquals(billingPurchase.getPurchaseToken(), purchase.token());
        assertEquals(billingPurchase.getSku(), purchase.product().sku());
        assertEquals(JSON_REFUNDED, purchase.receipt());
        assertTrue(purchase.refunded());
    }
}
