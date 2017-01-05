package com.getkeepsafe.cashier.iab;

import com.getkeepsafe.cashier.Product;

import org.json.JSONException;
import org.json.JSONObject;

import static com.getkeepsafe.cashier.iab.InAppBillingConstants.ProductConstants.CURRENCY;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.ProductConstants.DESCRIPTION;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.ProductConstants.NAME;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.ProductConstants.PRICE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.ProductConstants.PRICE_MICRO;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.ProductConstants.SKU;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.VENDOR_PACKAGE;

public class InAppBillingProduct {
    public static Product create(String skuDetailJson, boolean isSubscription) throws JSONException {
        final JSONObject json = new JSONObject(skuDetailJson);
        return create(json.getString(SKU),
                json.getString(PRICE),
                json.getString(CURRENCY),
                json.getString(NAME),
                json.getString(DESCRIPTION),
                isSubscription,
                Long.parseLong(json.getString(PRICE_MICRO)));
    }

    public static Product create(String sku,
                                 String price,
                                 String currency,
                                 String name,
                                 String description,
                                 boolean isSubscription,
                                 long microsPrice) {
        return Product.create(
                VENDOR_PACKAGE,
                sku,
                price,
                currency,
                name,
                description,
                isSubscription,
                microsPrice);
    }

    private InAppBillingProduct() {}
}
