package com.getkeepsafe.cashier.iab;

import android.content.Intent;
import android.os.Parcelable;

import com.getkeepsafe.cashier.CashierPurchase;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;
import com.google.auto.value.AutoValue;

import org.json.JSONException;
import org.json.JSONObject;

import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.*;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.*;

@AutoValue
public abstract class InAppBillingPurchase implements Parcelable, Purchase {
    public static final String GP_KEY_PACKAGE_NAME = "gp-package-name";
    public static final String GP_KEY_DATA_SIG = "gp-data-signature";
    public static final String GP_KEY_AUTO_RENEW = "gp-auto-renewing";
    public static final String GP_KEY_PURCHASE_TIME = "gp-purchase-time";
    public static final String GP_KEY_PURCHASE_STATE = "gp-purchase-state";
    public static final String GP_KEY_PURCHASE_DATA = "gp-purchase-data";

    public abstract Purchase purchase();

    /** The application package from which the purchase originated */
    public abstract String packageName();

    /**
     * String containing the signature of the purchase data that was signed with the private key
     * of the developer.
     */
    public abstract String dataSignature();

    /**
     * Indicates whether a subscription renews automatically. {@code false} indicates a canceled
     * subscription.
     */
    public abstract boolean autoRenewing();

    /**
     * The time the product was purchased, in milliseconds since the UNIX epoch
     */
    public abstract long purchaseTime();

    /** The purchase state of the order.
     * Possible values are:
     * <ul>
     *     <li>{@code 0} - Purchased</li>
     *     <li>{@code 1} - Canceled</li>
     *     <li>{@code 2} - Refunded</li>
     * </ul>
     */
    public abstract int purchaseState();

    /**
     * The original purchase data receipt from Google Play. This is useful for data signature
     * validation
     */
    public abstract String receipt();

    public Product product() {
        return purchase().product();
    }

    public String orderId() {
        return purchase().orderId();
    }

    public String token() {
        return purchase().token();
    }

    public String developerPayload() {
        return purchase().developerPayload();
    }

    public static InAppBillingPurchase create(String json) throws JSONException {
        return create(new JSONObject(json));
    }

    public static InAppBillingPurchase create(JSONObject json) throws JSONException {
        final Purchase purchase = CashierPurchase.create(json);
        final String packageName = json.getString(GP_KEY_PACKAGE_NAME);
        final String dataSignature = json.getString(GP_KEY_DATA_SIG);
        final String purchaseData = json.getString(GP_KEY_PURCHASE_DATA);
        final boolean autoRenew = json.getBoolean(GP_KEY_AUTO_RENEW);
        final long purchaseTime = json.getLong(GP_KEY_PURCHASE_TIME);
        final int purchaseState = json.getInt(GP_KEY_PURCHASE_STATE);

        return create(purchase,
                packageName,
                dataSignature,
                autoRenew,
                purchaseTime,
                purchaseState,
                purchaseData);
    }

    public static InAppBillingPurchase create(Product product, Intent purchaseIntent)
            throws JSONException {
        if (product == null || purchaseIntent == null) {
            throw new IllegalArgumentException("Product or purchase intent is null");
        }

        final String purchaseData = purchaseIntent.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA);
        final String dataSignature = purchaseIntent.getStringExtra(RESPONSE_INAPP_SIGNATURE);

        if (purchaseData == null || dataSignature == null) {
            throw new IllegalStateException("The given purchase intent is malformed");
        }

        return create(product, purchaseData, dataSignature);
    }

    public static InAppBillingPurchase create(Product product, String purchaseData,
                                              String dataSignature) throws JSONException {
        if (product == null || purchaseData == null || dataSignature == null) {
            throw new IllegalArgumentException("Product or purchase data or signature is null");
        }

        final JSONObject data = new JSONObject(purchaseData);
        final String packageName = data.getString(PACKAGE_NAME);
        final String purchaseToken = data.getString(PURCHASE_TOKEN);
        final String developerPayload = data.optString(DEVELOPER_PAYLOAD, "");
        final String orderId = data.getString(ORDER_ID);
        final String sku = data.getString(PRODUCT_ID);
        if (!sku.equals(product.sku())) {
            throw new IllegalArgumentException("Received mismatched SKU! "
                    + sku + " vs " + product.sku());
        }

        final boolean autoRenewing = data.optBoolean(AUTO_RENEWING, false);
        final long purchaseTime = data.getLong(PURCHASE_TIME);
        final int purchaseState = data.getInt(PURCHASE_STATE);

        final Purchase purchase =
                CashierPurchase.create(product, orderId, purchaseToken, purchaseData, developerPayload);

        return create(
                purchase,
                packageName,
                dataSignature,
                autoRenewing,
                purchaseTime,
                purchaseState,
                purchaseData);
    }

    public static InAppBillingPurchase create(Purchase purchase, String packageName,
                                              String dataSignature, boolean autoRenew,
                                              long purchaseTime, int purchaseState,
                                              String purchaseData) {
        return new AutoValue_InAppBillingPurchase(purchase,
                packageName,
                dataSignature,
                autoRenew,
                purchaseTime,
                purchaseState,
                purchaseData);
    }


    public boolean purchased() {
        return purchaseState() == PURCHASE_STATE_PURCHASED;
    }

    public boolean canceled() {
        return purchaseState() == PURCHASE_STATE_CANCELED;
    }

    public boolean refunded() {
        return purchaseState() == PURCHASE_STATE_REFUNDED;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        final JSONObject object = purchase().toJson();
        object.put(GP_KEY_PACKAGE_NAME, packageName());
        object.put(GP_KEY_DATA_SIG, dataSignature());
        object.put(GP_KEY_AUTO_RENEW, autoRenewing());
        object.put(GP_KEY_PURCHASE_TIME, purchaseTime());
        object.put(GP_KEY_PURCHASE_STATE, purchaseState());
        object.put(GP_KEY_PURCHASE_DATA, receipt());

        return object;
    }
}
