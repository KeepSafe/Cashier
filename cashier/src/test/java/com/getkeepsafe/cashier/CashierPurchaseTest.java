package com.getkeepsafe.cashier;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
public class CashierPurchaseTest {
  final CashierPurchase purchase = ValueFactory.aPurchase();

  @Test
  public void acceptsValidJson() throws JSONException {
    CashierPurchase.create(
        "{\"micros-price\":1," +
            "\"cashier-token\":\"a\"," +
            "\"vendor-id\":\"a\"," +
            "\"cashier-developer-payload\":\"a\"," +
            "\"price\":\"1\"," +
            "\"name\":\"a\"," +
            "\"cashier-order-id\":\"a\"," +
            "\"description\":\"a\"," +
            "\"currency\":\"a\"," +
            "\"subscription\":true," +
            "\"sku\":\"a\"," +
            "\"cashier-receipt\":\"a\"}"
    );
  }

  @Test(expected = JSONException.class)
  public void rejectsInvalidJson() throws JSONException {
    CashierPurchase.create("bad json");
  }

  @Test
  public void serializesToJson() throws JSONException {
    assertJsonHasProperties(purchase.toJson(), purchase);
  }

  @Test
  public void deserializesFromJson() throws JSONException {
    assertThat(CashierPurchase.create(purchase.toJson())).isEqualTo(purchase);
  }

  @Test
  public void isParcelable() {
    final Bundle bundle = new Bundle();
    final String key = "purchase";
    bundle.putParcelable(key, purchase);
    assertThat(bundle.getParcelable(key)).isEqualTo(purchase);
  }

  static void assertJsonHasProperties(JSONObject json, CashierPurchase purchase) throws JSONException {
    ProductTest.assertJsonHasProperties(json, purchase.product());
    assertThat(json.getString(CashierPurchase.KEY_ORDER_ID)).isEqualTo(purchase.orderId());
    assertThat(json.getString(CashierPurchase.KEY_TOKEN)).isEqualTo(purchase.token());
    assertThat(json.getString(CashierPurchase.KEY_RECEIPT)).isEqualTo(purchase.receipt());
    assertThat(json.getString(CashierPurchase.KEY_DEV_PAYLOAD)).isEqualTo(purchase.developerPayload());
  }
}
