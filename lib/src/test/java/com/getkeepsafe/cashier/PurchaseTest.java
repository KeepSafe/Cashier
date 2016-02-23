package com.getkeepsafe.cashier;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(RobolectricGradleTestRunner.class)
public class PurchaseTest extends ProductTest {
    protected final String ORDER_ID = "order123";
    protected final String TOKEN = "token123";
    protected final String DEV_PAYLOAD = "payload123";

    @Before
    public void setUp() throws JSONException {
        super.setUp();
        jsonObject.put(Purchase.KEY_ORDER_ID, ORDER_ID);
        jsonObject.put(Purchase.KEY_TOKEN, TOKEN);
        jsonObject.put(Purchase.KEY_DEV_PAYLOAD, DEV_PAYLOAD);
    }

    @Test
    public void serializesToJson() throws JSONException {
        final Product product =
                new Product(VENDOR, SKU, PRICE, CURRENCY, NAME, DESCRIPTION, IS_SUB, MICRO_PRICE);
        final Purchase purchase = new Purchase(product, ORDER_ID, TOKEN, DEV_PAYLOAD);
        final JSONObject json = purchase.serializeToJson();
        assertJsonHasProperties(json);
    }

    @Override
    protected void assertJsonHasProperties(final JSONObject json) throws JSONException {
        super.assertJsonHasProperties(json);
        assertThat(json.getString(Purchase.KEY_ORDER_ID), is(ORDER_ID));
        assertThat(json.getString(Purchase.KEY_TOKEN), is(TOKEN));
        assertThat(json.getString(Purchase.KEY_DEV_PAYLOAD), is(DEV_PAYLOAD));
    }

    @Test
    public void deserializesFromJson() throws JSONException {
        final String json = jsonObject.toString();
        final Purchase purchase = new Purchase(json);
        assertHasProperties(purchase);
    }

    protected void assertHasProperties(final Purchase purchase) {
        super.assertHasProperties(purchase);
        assertThat(purchase.orderId, is(ORDER_ID));
        assertThat(purchase.token, is(TOKEN));
        assertThat(purchase.developerPayload, is(DEV_PAYLOAD));
    }

    @Test
    public void parcelable() {
        final Product product =
                new Product(VENDOR, SKU, PRICE, CURRENCY, NAME, DESCRIPTION, IS_SUB, MICRO_PRICE);
        final Purchase purchase = new Purchase(product, ORDER_ID, TOKEN, DEV_PAYLOAD);
        final Bundle bundle = new Bundle();
        final String key = "purchase";
        bundle.putParcelable(key, purchase);
        assertThat((Purchase) bundle.getParcelable(key), is(purchase));
    }
}
