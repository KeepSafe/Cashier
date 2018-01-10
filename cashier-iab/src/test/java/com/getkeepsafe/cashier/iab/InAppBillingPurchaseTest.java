package com.getkeepsafe.cashier.iab;

import android.os.Bundle;

import com.getkeepsafe.cashier.Product;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.PURCHASE_STATE_CANCELED;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.PURCHASE_STATE_PURCHASED;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.PURCHASE_STATE_REFUNDED;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.IN_APP_BILLING_PURCHASE_VALID_PRODUCT_JSON;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.VALID_ONE_TIME_PURCHASE_JSON;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.VALID_PURCHASE_RECEIPT_JSON;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.VALID_TEST_PURCHASE_RECEIPT_JSON;
import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
public class InAppBillingPurchaseTest {

  @Test
  public void suppliesTestOrderIdWhenMissing() throws JSONException {
    Product testProduct = Product.create(IN_APP_BILLING_PURCHASE_VALID_PRODUCT_JSON);
    InAppBillingPurchase purchase = InAppBillingPurchase.create(testProduct, VALID_TEST_PURCHASE_RECEIPT_JSON, "test");
    assertThat(purchase.orderId()).isEqualTo(InAppBillingPurchase.GP_ORDER_ID_TEST);
    assertThat(purchase.isTestPurchase()).isTrue();
  }

  @Test
  public void doesNotSupplyTestOrderIdByDefault() throws JSONException {
    Product testProduct = Product.create(IN_APP_BILLING_PURCHASE_VALID_PRODUCT_JSON);
    InAppBillingPurchase purchase = InAppBillingPurchase.create(testProduct, VALID_PURCHASE_RECEIPT_JSON, "test");
    assertThat(purchase.orderId()).isNotEqualTo(InAppBillingPurchase.GP_ORDER_ID_TEST);
  }

  @Test
  public void acceptsValidJson() throws JSONException {
    Product product = Product.create(IN_APP_BILLING_PURCHASE_VALID_PRODUCT_JSON);
    InAppBillingPurchase.create(product, VALID_PURCHASE_RECEIPT_JSON, "test");
    InAppBillingPurchase.create(VALID_ONE_TIME_PURCHASE_JSON);
  }

  @Test(expected = JSONException.class)
  public void rejectsInvalidJson() throws JSONException {
    InAppBillingPurchase.create("bad json");
  }

  @Test
  public void serializesAndDeserializesCorrectly() throws JSONException {
    Product product = Product.create(IN_APP_BILLING_PURCHASE_VALID_PRODUCT_JSON);
    final String json = InAppBillingPurchase.create(product, VALID_PURCHASE_RECEIPT_JSON, "test").toJson().toString();
    InAppBillingPurchase.create(json);
  }

  @Test
  public void serializesPropertiesCorrectly() throws JSONException {
    Product product = Product.create(IN_APP_BILLING_PURCHASE_VALID_PRODUCT_JSON);
    InAppBillingPurchase purchase = InAppBillingPurchase.create(product, VALID_PURCHASE_RECEIPT_JSON, "test");
    assertJsonHasProperties(purchase.toJson(), purchase);
  }

  @Test
  public void deserializesPropertiesCorrectly() throws JSONException {
    Product product = Product.create(IN_APP_BILLING_PURCHASE_VALID_PRODUCT_JSON);
    InAppBillingPurchase purchase = InAppBillingPurchase.create(product, VALID_PURCHASE_RECEIPT_JSON, "test");
    assertThat(InAppBillingPurchase.create(purchase.toJson())).isEqualTo(purchase);
  }

  @Test
  public void isParcelable() throws JSONException {
    Product product = Product.create(IN_APP_BILLING_PURCHASE_VALID_PRODUCT_JSON);
    InAppBillingPurchase purchase = InAppBillingPurchase.create(product, VALID_PURCHASE_RECEIPT_JSON, "test");
    Bundle bundle = new Bundle();
    String key = "purchase";
    bundle.putParcelable(key, purchase);
    assertThat(bundle.getParcelable(key)).isEqualTo(purchase);
  }

  @Test
  public void purchasedState() throws JSONException {
    Product product = Product.create(IN_APP_BILLING_PURCHASE_VALID_PRODUCT_JSON);
    assertThat(InAppBillingPurchase.create(product, receiptWithState(PURCHASE_STATE_PURCHASED), "a").purchased()).isTrue();
    assertThat(InAppBillingPurchase.create(product, receiptWithState(PURCHASE_STATE_REFUNDED), "a").purchased()).isFalse();
    assertThat(InAppBillingPurchase.create(product, receiptWithState(PURCHASE_STATE_CANCELED), "a").purchased()).isFalse();
  }

  @Test
  public void canceledState() throws JSONException {
    Product product = Product.create(IN_APP_BILLING_PURCHASE_VALID_PRODUCT_JSON);
    assertThat(InAppBillingPurchase.create(product, receiptWithState(PURCHASE_STATE_PURCHASED), "a").canceled()).isFalse();
    assertThat(InAppBillingPurchase.create(product, receiptWithState(PURCHASE_STATE_REFUNDED), "a").canceled()).isFalse();
    assertThat(InAppBillingPurchase.create(product, receiptWithState(PURCHASE_STATE_CANCELED), "a").canceled()).isTrue();
  }

  @Test
  public void refundedState() throws JSONException {
    Product product = Product.create(IN_APP_BILLING_PURCHASE_VALID_PRODUCT_JSON);
    assertThat(InAppBillingPurchase.create(product, receiptWithState(PURCHASE_STATE_PURCHASED), "a").refunded()).isFalse();
    assertThat(InAppBillingPurchase.create(product, receiptWithState(PURCHASE_STATE_REFUNDED), "a").refunded()).isTrue();
    assertThat(InAppBillingPurchase.create(product, receiptWithState(PURCHASE_STATE_CANCELED), "a").refunded()).isFalse();
  }

  static String receiptWithState(int state) {
    return "{\"orderId\":\"testtest\"," +
        "\"autoRenewing\":false," +
        "\"packageName\":\"com.getkeepsafe.cashier.sample\"," +
        "\"productId\":\"so.product.much.purchase\"," +
        "\"purchaseTime\":1476077957823," +
        "\"purchaseState\":" + state + "," +
        "\"developerPayload\":\"hello-cashier!\"," +
        "\"purchaseToken\":\"15d12f9b-82fc-4977-b49c-aef730a10463\"}";
  }

  static void assertJsonHasProperties(JSONObject json, InAppBillingPurchase purchase) throws JSONException {
    assertThat(json.getString(InAppBillingPurchase.GP_KEY_PACKAGE_NAME)).isEqualTo(purchase.packageName());
    assertThat(json.getBoolean(InAppBillingPurchase.GP_KEY_AUTO_RENEW)).isEqualTo(purchase.autoRenewing());
    assertThat(json.getString(InAppBillingPurchase.GP_KEY_DATA_SIG)).isEqualTo(purchase.dataSignature());
    assertThat(json.getString(InAppBillingPurchase.GP_KEY_PURCHASE_DATA)).isEqualTo(purchase.receipt());
    assertThat(json.getInt(InAppBillingPurchase.GP_KEY_PURCHASE_STATE)).isEqualTo(purchase.purchaseState());
    assertThat(json.getLong(InAppBillingPurchase.GP_KEY_PURCHASE_TIME)).isEqualTo(purchase.purchaseTime());
  }
}
