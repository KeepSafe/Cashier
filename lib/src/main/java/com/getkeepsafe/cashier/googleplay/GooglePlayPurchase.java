package com.getkeepsafe.cashier.googleplay;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;


import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;
import org.json.JSONObject;

public class GooglePlayPurchase extends Purchase
        implements GooglePlayConstants, GooglePlayConstants.PurchaseConstants, Parcelable {
    public static final String GP_KEY_PACKAGE_NAME = "gp-package-name";
    public static final String GP_KEY_DATA_SIG = "gp-data-signature";
    public static final String GP_KEY_AUTO_RENEW = "gp-auto-renewing";
    public static final String GP_KEY_PURCHASE_TIME = "gp-purchase-time";
    public static final String GP_KEY_PURCHASE_STATE = "gp-purchase-state";
    public static final String GP_KEY_PURCHASE_DATA = "gp-purchase-data";

    /** The application package from which the purchase originated */
    public final String packageName;

    /**
     * String containing the signature of the purchase data that was signed with the private key
     * of the developer.
     */
    public final String dataSignature;

    /**
     * Indicates whether a subscription renews automatically. {@code false} indicates a canceled
     * subscription.
     */
    public final boolean autoRenewing;

    /**
     * The time the product was purchased, in milliseconds since the UNIX epoch
     */
    public final long purchaseTime;

    /** The purchase state of the order.
     * Possible values are:
     * <ul>
     *     <li>{@code 0} - Purchased</li>
     *     <li>{@code 1} - Canceled</li>
     *     <li>{@code 2} - Refunded</li>
     * </ul>
     */
    public final int purchaseState;

    /**
     * The original purchase data receipt from Google Play. This is useful for data signature
     * validation
     */
    public final String purchaseData;

    public static GooglePlayPurchase of(final Product product,
                                        final Intent purchaseIntent)
            throws JSONException {
        Check.notNull(product, "Product");
        final String purchaseData = Check.notNull(purchaseIntent, "Purchase Intent")
                .getStringExtra(RESPONSE_INAPP_PURCHASE_DATA);
        final String dataSignature = purchaseIntent.getStringExtra(RESPONSE_INAPP_SIGNATURE);

        return of(product,
                Check.notNull(purchaseData, "Purchase Data"),
                Check.notNull(dataSignature, "Purchase Data"));
    }

    public static GooglePlayPurchase of(final Product product,
                                        final String purchaseData,
                                        final String dataSignature) throws JSONException {
        Check.notNull(product, "Product");
        Check.notNull(purchaseData, "Purchase Data");
        Check.notNull(dataSignature, "Signature");
        final JSONObject data = new JSONObject(purchaseData);
        final String packageName = data.getString(PACKAGE_NAME);
        final String purchaseToken = data.getString(PURCHASE_TOKEN);
        final String developerPayload = data.optString(DEVELOPER_PAYLOAD, "");
        final String orderId = data.getString(ORDER_ID);
        final String sku = data.getString(PRODUCT_ID);
        if (!sku.equals(product.sku)) {
            throw new IllegalArgumentException("Received mismatched SKU! "
                    + sku + " vs " + product.sku);
        }

        final boolean autoRenewing = data.optBoolean(AUTO_RENEWING, false);
        final long purchaseTime = data.getLong(PURCHASE_TIME);
        final int purchaseState = data.getInt(PURCHASE_STATE);

        return new GooglePlayPurchase(
                product,
                orderId,
                purchaseToken,
                packageName,
                dataSignature,
                developerPayload,
                purchaseData,
                purchaseTime,
                purchaseState,
                autoRenewing);
    }

    private GooglePlayPurchase(final Product product,
                               final String orderId,
                               final String token,
                               final String packageName,
                               final String dataSignature,
                               final String developerPayload,
                               final String purchaseData,
                               final long purchaseTime,
                               final int purchaseState,
                               final boolean autoRenewing) throws JSONException {
        super(product, orderId, token, developerPayload);
        this.packageName = Check.notNull(packageName);
        this.dataSignature = Check.notNull(dataSignature);
        this.purchaseData = Check.notNull(purchaseData);
        this.autoRenewing = autoRenewing;
        this.purchaseTime = purchaseTime;
        this.purchaseState = purchaseState;
    }

    public GooglePlayPurchase(final String json) throws JSONException {
        this(new JSONObject(Check.notNull(json, "Google Play Purchase JSON")));
    }

    public GooglePlayPurchase(final JSONObject json) throws JSONException {
        super(json);
        packageName = json.getString(GP_KEY_PACKAGE_NAME);
        dataSignature = json.getString(GP_KEY_DATA_SIG);
        autoRenewing = json.getBoolean(GP_KEY_AUTO_RENEW);
        purchaseTime = json.getLong(GP_KEY_PURCHASE_TIME);
        purchaseState = json.getInt(GP_KEY_PURCHASE_STATE);
        purchaseData = json.getString(GP_KEY_PURCHASE_DATA);
    }

    public boolean purchased() {
        return purchaseState == 0;
    }

    public boolean canceled() {
        return purchaseState == 1;
    }

    public boolean refunded() {
        return purchaseState == 2;
    }

    @Override
    protected JSONObject serializeToJson() throws JSONException {
        final JSONObject object = super.serializeToJson();
        object.put(GP_KEY_PACKAGE_NAME, packageName);
        object.put(GP_KEY_DATA_SIG, dataSignature);
        object.put(GP_KEY_AUTO_RENEW, autoRenewing);
        object.put(GP_KEY_PURCHASE_TIME, purchaseTime);
        object.put(GP_KEY_PURCHASE_STATE, purchaseState);
        object.put(GP_KEY_PURCHASE_DATA, purchaseData);

        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GooglePlayPurchase that = (GooglePlayPurchase) o;

        if (autoRenewing != that.autoRenewing) return false;
        if (purchaseTime != that.purchaseTime) return false;
        if (purchaseState != that.purchaseState) return false;
        if (!purchaseData.equals(that.purchaseData)) return false;
        if (!packageName.equals(that.packageName)) return false;
        return dataSignature.equals(that.dataSignature);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + packageName.hashCode();
        result = 31 * result + dataSignature.hashCode();
        result = 31 * result + (autoRenewing ? 1 : 0);
        result = 31 * result + (int) (purchaseTime ^ (purchaseTime >>> 32));
        result = 31 * result + purchaseState;
        result = 31 * result + purchaseData.hashCode();
        return result;
    }

    // Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.packageName);
        dest.writeString(this.dataSignature);
        dest.writeByte(autoRenewing ? (byte) 1 : (byte) 0);
        dest.writeLong(this.purchaseTime);
        dest.writeInt(this.purchaseState);
        dest.writeString(this.purchaseData);
    }

    protected GooglePlayPurchase(Parcel in) {
        super(in);
        this.packageName = in.readString();
        this.dataSignature = in.readString();
        this.autoRenewing = in.readByte() != 0;
        this.purchaseTime = in.readLong();
        this.purchaseState = in.readInt();
        this.purchaseData = in.readString();
    }

    public static final Creator<GooglePlayPurchase> CREATOR = new Creator<GooglePlayPurchase>() {
        public GooglePlayPurchase createFromParcel(Parcel source) {
            return new GooglePlayPurchase(source);
        }

        public GooglePlayPurchase[] newArray(int size) {
            return new GooglePlayPurchase[size];
        }
    };
}
