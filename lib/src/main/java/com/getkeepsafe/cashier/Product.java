package com.getkeepsafe.cashier;

import android.os.Parcel;
import android.os.Parcelable;


import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;
import org.json.JSONObject;

public class Product implements Parcelable {
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

    public Product(final String json) throws JSONException {
        this(new JSONObject(Check.notNull(json, "Product JSON")));
    }

    public Product(final JSONObject json) throws JSONException {
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

    public Product(final String vendorId,
                   final String sku,
                   final String price,
                   final String currency,
                   final String name,
                   final String description,
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

    public Product(final Product product) {
        this(product.vendorId,
                product.sku,
                product.price,
                product.currency,
                product.name,
                product.description,
                product.isSubscription,
                product.microsPrice);
    }

    protected JSONObject serializeToJson() throws JSONException {
        final JSONObject object = new JSONObject();
        object.put(KEY_VENDOR_ID, vendorId);
        object.put(KEY_NAME, name);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        if (isSubscription != product.isSubscription) return false;
        if (microsPrice != product.microsPrice) return false;
        if (!vendorId.equals(product.vendorId)) return false;
        if (!sku.equals(product.sku)) return false;
        if (!price.equals(product.price)) return false;
        if (!currency.equals(product.currency)) return false;
        if (!name.equals(product.name)) return false;
        return description.equals(product.description);

    }

    @Override
    public int hashCode() {
        int result = vendorId.hashCode();
        result = 31 * result + sku.hashCode();
        result = 31 * result + price.hashCode();
        result = 31 * result + currency.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + (isSubscription ? 1 : 0);
        result = 31 * result + (int) (microsPrice ^ (microsPrice >>> 32));
        return result;
    }

    // Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.vendorId);
        dest.writeString(this.sku);
        dest.writeString(this.price);
        dest.writeString(this.currency);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeByte(isSubscription ? (byte) 1 : (byte) 0);
        dest.writeLong(this.microsPrice);
    }

    protected Product(Parcel in) {
        this.vendorId = in.readString();
        this.sku = in.readString();
        this.price = in.readString();
        this.currency = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.isSubscription = in.readByte() != 0;
        this.microsPrice = in.readLong();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
}
