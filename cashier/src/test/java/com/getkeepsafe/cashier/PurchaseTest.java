package com.getkeepsafe.cashier;

import android.os.Bundle;

import com.getkeepsafe.LibraryProjectRobolectricTestRunner;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LibraryProjectRobolectricTestRunner.class)
public class PurchaseTest extends ProductTest {
    protected final String ORDER_ID = "order123";
    protected final String TOKEN = "token123";
    protected final String DEV_PAYLOAD = "payload123";

    @Before
    public void setUp() throws JSONException {
        super.setUp();
        jsonObject.put(CashierPurchase.KEY_ORDER_ID, ORDER_ID);
        jsonObject.put(CashierPurchase.KEY_TOKEN, TOKEN);
        jsonObject.put(CashierPurchase.KEY_DEV_PAYLOAD, DEV_PAYLOAD);
    }

    @Test
    public void serializesToJson() throws JSONException {
        final Product product = Product.create(VENDOR, SKU, PRICE, CURRENCY, NAME, DESCRIPTION, IS_SUB, MICRO_PRICE);
        final CashierPurchase purchase = CashierPurchase.create(product, ORDER_ID, TOKEN, DEV_PAYLOAD);
        final JSONObject json = purchase.toJson();
        assertJsonHasProperties(json);
    }

    @Override
    protected void assertJsonHasProperties(final JSONObject json) throws JSONException {
        super.assertJsonHasProperties(json);
        assertThat(json.getString(CashierPurchase.KEY_ORDER_ID)).isEqualTo(ORDER_ID);
        assertThat(json.getString(CashierPurchase.KEY_TOKEN)).isEqualTo(TOKEN);
        assertThat(json.getString(CashierPurchase.KEY_DEV_PAYLOAD)).isEqualTo(DEV_PAYLOAD);
    }

    @Test
    public void deserializesFromJson() throws JSONException {
        final String json = jsonObject.toString();
        final CashierPurchase purchase = CashierPurchase.create(json);
        assertHasProperties(purchase);
    }

    protected void assertHasProperties(final CashierPurchase purchase) {
        super.assertHasProperties(purchase.product());
        assertThat(purchase.orderId()).isEqualTo(ORDER_ID);
        assertThat(purchase.token()).isEqualTo(TOKEN);
        assertThat(purchase.developerPayload()).isEqualTo(DEV_PAYLOAD);
    }

    @Test
    public void parcelable() {
        final Product product =
                Product.create(VENDOR, SKU, PRICE, CURRENCY, NAME, DESCRIPTION, IS_SUB, MICRO_PRICE);
        final CashierPurchase purchase = CashierPurchase.create(product, ORDER_ID, TOKEN, DEV_PAYLOAD);
        final Bundle bundle = new Bundle();
        final String key = "purchase";
        bundle.putParcelable(key, purchase);
        assertThat((CashierPurchase) bundle.getParcelable(key)).isEqualTo(purchase);
    }
}
