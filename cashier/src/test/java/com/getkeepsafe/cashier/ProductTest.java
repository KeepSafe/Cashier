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
public class ProductTest {
    protected final String VENDOR = "fake-vendor";
    protected final String SKU = "fake-sku";
    protected final String PRICE = "$1337.00";
    protected final String CURRENCY = "USD";
    protected final String NAME = "1337 Product";
    protected final String DESCRIPTION = "A product that makes you 1337";
    protected final boolean IS_SUB = true;
    protected final long MICRO_PRICE = 1_337_000_000L;

    protected JSONObject jsonObject;

    @Before
    public void setUp() throws JSONException {
        jsonObject = new JSONObject();
        jsonObject.put(Product.KEY_VENDOR_ID, VENDOR);
        jsonObject.put(Product.KEY_SKU, SKU);
        jsonObject.put(Product.KEY_NAME, NAME);
        jsonObject.put(Product.KEY_PRICE, PRICE);
        jsonObject.put(Product.KEY_CURRENCY, CURRENCY);
        jsonObject.put(Product.KEY_DESCRIPTION, DESCRIPTION);
        jsonObject.put(Product.KEY_IS_SUB, IS_SUB);
        jsonObject.put(Product.KEY_MICRO_PRICE, MICRO_PRICE);
    }

    @Test
    public void serializesToJson() throws JSONException {
        final Product product = Product.create(VENDOR, SKU, PRICE, CURRENCY, NAME, DESCRIPTION, IS_SUB, MICRO_PRICE);
        final JSONObject json = product.toJson();
        assertJsonHasProperties(json);
    }

    protected void assertJsonHasProperties(final JSONObject json) throws JSONException {
        assertThat(json.getString(Product.KEY_VENDOR_ID)).isEqualTo(VENDOR);
        assertThat(json.getString(Product.KEY_SKU)).isEqualTo(SKU);
        assertThat(json.getString(Product.KEY_PRICE)).isEqualTo(PRICE);
        assertThat(json.getString(Product.KEY_CURRENCY)).isEqualTo(CURRENCY);
        assertThat(json.getString(Product.KEY_NAME)).isEqualTo(NAME);
        assertThat(json.getString(Product.KEY_DESCRIPTION)).isEqualTo(DESCRIPTION);
        assertThat(json.getBoolean(Product.KEY_IS_SUB)).isEqualTo(IS_SUB);
        assertThat(json.getLong(Product.KEY_MICRO_PRICE)).isEqualTo(MICRO_PRICE);
    }

    @Test
    public void deserializesFromJson() throws JSONException {
        final String json = jsonObject.toString();
        final Product product = Product.create(json);
        assertHasProperties(product);
    }

    protected void assertHasProperties(final Product product) {
        assertThat(product.vendorId()).isEqualTo(VENDOR);
        assertThat(product.sku()).isEqualTo(SKU);
        assertThat(product.price()).isEqualTo(PRICE);
        assertThat(product.currency()).isEqualTo(CURRENCY);
        assertThat(product.name()).isEqualTo(NAME);
        assertThat(product.description()).isEqualTo(DESCRIPTION);
        assertThat(product.isSubscription()).isEqualTo(IS_SUB);
        assertThat(product.microsPrice()).isEqualTo(MICRO_PRICE);
    }

    @Test
    public void parcelable() {
        final Product product = Product.create(VENDOR, SKU, PRICE, CURRENCY, NAME, DESCRIPTION, IS_SUB, MICRO_PRICE);
        final Bundle bundle = new Bundle();
        final String key = "product";
        bundle.putParcelable(key, product);
        assertThat((Product) bundle.getParcelable(key)).isEqualTo(product);
    }
}
