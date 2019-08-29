package com.getkeepsafe.cashier.billing.debug;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.getkeepsafe.cashier.Product;

import org.json.JSONException;

/**
 * Fake sku details query result. SkuDetails requires json to be constructed,
 * this class overrides mostly used fields to read values from Product directly.
 */
public class FakeSkuDetails extends SkuDetails {

    private Product product;

    public FakeSkuDetails(Product product) throws JSONException {
        super("{}");
        this.product = product;
    }

    @Override
    public String getTitle() {
        return product.name();
    }

    @Override
    public String getDescription() {
        return product.description();
    }

    @Override
    public String getSku() {
        return product.sku();
    }

    @Override
    public String getType() {
        return product.isSubscription() ? BillingClient.SkuType.SUBS : BillingClient.SkuType.INAPP;
    }

    @Override
    public String getPrice() {
        return product.price();
    }

    @Override
    public long getPriceAmountMicros() {
        return product.microsPrice();
    }

    @Override
    public String getPriceCurrencyCode() {
        return product.currency();
    }
}
