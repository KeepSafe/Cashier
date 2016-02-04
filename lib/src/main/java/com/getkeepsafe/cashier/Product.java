package com.getkeepsafe.cashier;

import android.support.annotation.NonNull;

import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;
import org.json.JSONObject;

public class Product {
    public static final String KEY_VENDOR_ID = "vendor-id";
    public static final String KEY_SKU = "sku";
    public static final String KEY_PRICE = "price";
    public static final String KEY_CURRENCY = "currency";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IS_SUB = "subscription";
    public static final String KEY_MICRO_PRICE = "micros-price";

    public final String vendorId;
    public final String sku;
    public final String price;
    public final String currency;
    public final String name;
    public final String description;
    public final boolean isSubscription;
    public final long microsPrice;

    public Product(@NonNull final String json) throws JSONException {
        this(new JSONObject(Check.notNull(json, "Product JSON")));
    }

    public Product(@NonNull final JSONObject json) throws JSONException {
        Check.notNull(json, "Product JSON");

        vendorId = json.getString(KEY_VENDOR_ID);
        sku = json.getString(KEY_SKU);
        price = json.getString(KEY_PRICE);
        currency = json.getString(KEY_CURRENCY);
        name = json.getString(KEY_NAME);
        description = json.getString(KEY_DESCRIPTION);
        isSubscription = json.getBoolean(KEY_IS_SUB);
        microsPrice = json.getLong(KEY_MICRO_PRICE);
    }

    public Product(@NonNull final String vendorId,
                   @NonNull final String sku,
                   @NonNull final String price,
                   @NonNull final String currency,
                   @NonNull final String name,
                   @NonNull final String description,
                   final boolean isSubscription,
                   final long microsPrice) {
        this.vendorId = Check.notNull(vendorId, "Vendor ID");
        this.sku = Check.notNull(sku, "SKU");
        this.price = Check.notNull(price, "Price");
        this.currency = Check.notNull(currency, "Currency");
        this.name = Check.notNull(name, "Product Name");
        this.description = Check.notNull(description, "Product Description");
        this.isSubscription = isSubscription;
        this.microsPrice = microsPrice;
    }

    public Product(@NonNull final Product product) {
        this(product.vendorId,
                product.sku,
                product.price,
                product.currency,
                product.name,
                product.description,
                product.isSubscription,
                product.microsPrice);
    }

    @NonNull
    protected JSONObject serializeToJson() throws JSONException {
        final JSONObject object = new JSONObject();
        object.put(KEY_VENDOR_ID, vendorId);
        object.put(KEY_SKU, sku);
        object.put(KEY_PRICE, price);
        object.put(KEY_CURRENCY, currency);
        object.put(KEY_DESCRIPTION, description);
        object.put(KEY_IS_SUB, isSubscription);
        object.put(KEY_MICRO_PRICE, microsPrice);
        return object;
    }

    public String toJson() throws JSONException {
        return serializeToJson().toString();
    }
}
