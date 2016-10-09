package com.getkeepsafe.cashier.googleplay;



import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;
import org.json.JSONObject;

public class GooglePlayProduct
        implements GooglePlayConstants, GooglePlayConstants.ProductConstants {
    public static Product of(final String skuDetailJson,
                             final boolean isSubscription) throws JSONException {
        final JSONObject json = new JSONObject(Check.notNull(skuDetailJson, "SKU JSON"));
        final String sku = json.getString(SKU);
        final String price = json.getString(PRICE);
        final String currency = json.getString(CURRENCY);
        final String name = json.getString(NAME);
        final String description = json.getString(DESCRIPTION);
        // TODO: Can this be a long instead?
        final String microsPrice = json.getString(PRICE_MICRO);

        return new Product(
                VENDOR_PACKAGE,
                sku,
                price,
                currency,
                name,
                description,
                isSubscription,
                Long.parseLong(microsPrice));
    }

    public static Product of(final String sku,
                             final String price,
                             final String currency,
                             final String name,
                             final String description,
                             final boolean isSubscription,
                             final long microsPrice) {
        return new Product(
                VENDOR_PACKAGE,
                sku,
                price,
                currency,
                name,
                description,
                isSubscription,
                microsPrice);
    }

    private GooglePlayProduct() {}
}
