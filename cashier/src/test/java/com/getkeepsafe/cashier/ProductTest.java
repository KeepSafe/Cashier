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
    final Product product = ValueFactory.aProduct();

    @Test
    public void acceptsValidJson() throws JSONException {
        Product.create(
                "{\"micros-price\":1," +
                "\"vendor-id\":\"1\"," +
                "\"price\":\"1\"," +
                "\"name\":\"1\"," +
                "\"description\":\"1\"," +
                "\"currency\":\"1\"," +
                "\"subscription\":false," +
                "\"sku\":\"1\"}"
        );
    }

    @Test(expected = JSONException.class)
    public void rejectsInvalidJson() throws JSONException {
        Product.create("bad json");
    }

    @Test
    public void serializesToJson() throws JSONException {
        assertJsonHasProperties(product.toJson(), product);
    }

    @Test
    public void deserializesFromJson() throws JSONException {
        assertThat(Product.create(product.toJsonString())).isEqualTo(product);
    }

    @Test
    public void isParcelable() {
        final Bundle bundle = new Bundle();
        final String key = "product";
        bundle.putParcelable(key, product);
        assertThat((Product) bundle.getParcelable(key)).isEqualTo(product);
    }

    static void assertJsonHasProperties(JSONObject json, Product product) throws JSONException {
        assertThat(json.getString(Product.KEY_VENDOR_ID)).isEqualTo(product.vendorId());
        assertThat(json.getString(Product.KEY_SKU)).isEqualTo(product.sku());
        assertThat(json.getString(Product.KEY_PRICE)).isEqualTo(product.price());
        assertThat(json.getString(Product.KEY_CURRENCY)).isEqualTo(product.currency());
        assertThat(json.getString(Product.KEY_NAME)).isEqualTo(product.name());
        assertThat(json.getString(Product.KEY_DESCRIPTION)).isEqualTo(product.description());
        assertThat(json.getBoolean(Product.KEY_IS_SUB)).isEqualTo(product.isSubscription());
        assertThat(json.getLong(Product.KEY_MICRO_PRICE)).isEqualTo(product.microsPrice());
    }
}
