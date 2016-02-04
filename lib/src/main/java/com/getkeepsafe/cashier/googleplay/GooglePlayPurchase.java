package com.getkeepsafe.cashier.googleplay;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;
import org.json.JSONObject;

public class GooglePlayPurchase extends Purchase implements GooglePlayConstants {
    /** The application package from which the purchase originated */
    public final String packageName;

    /**
     * String containing the signature of the purchase data that was signed with the private key
     * of the developer.
     */
    public final String dataSignature;

    /** A developer-specified string that contains supplemental information about an order. */
    public final String developerPayload;

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

    public static GooglePlayPurchase of(@NonNull final Product product,
                                        @NonNull final Intent purchaseIntent)
            throws JSONException {
        Check.notNull(product, "Product");
        final String purchaseData = Check.notNull(purchaseIntent, "Purchase Intent")
                .getStringExtra(RESPONSE_INAPP_PURCHASE_DATA);
        final String dataSignature = purchaseIntent.getStringExtra(RESPONSE_INAPP_SIGNATURE);

        return of(product,
                Check.notNull(purchaseData, "Purchase Data"),
                Check.notNull(dataSignature, "Purchase Data"));
    }

    public static GooglePlayPurchase of(@NonNull final Product product,
                                        @NonNull final String purchaseData,
                                        @NonNull final String dataSignature) throws JSONException {
        Check.notNull(product, "Product");
        Check.notNull(purchaseData, "Purchase Data");
        Check.notNull(dataSignature, "Signature");
        final JSONObject data = new JSONObject(purchaseData);
        final String packageName = data.getString("packageName");
        final String purchaseToken = data.getString("purchaseToken");
        final String developerPayload = data.optString("developerPayload");
        final String orderId = data.getString("orderId");
        final String sku = data.getString("productId");
        if (!sku.equals(product.sku)) {
            throw new IllegalArgumentException("Received mismatched SKU! "
                    + sku + " vs " + product.sku);
        }

        final boolean autoRenewing = data.optBoolean("autoRenewing", false);
        final long purchaseTime = data.getLong("purchaseTime");
        final int purchaseState = data.getInt("purchaseState");

        final GooglePlayPurchase purchase = new GooglePlayPurchase(
                product,
                orderId,
                purchaseToken,
                packageName,
                dataSignature,
                developerPayload,
                purchaseTime,
                purchaseState,
                autoRenewing);
        purchase.setExtras(purchaseData);

        return purchase;
    }

    private GooglePlayPurchase(@NonNull final Product product,
                               @NonNull final String orderId,
                               @NonNull final String token,
                               @NonNull final String packageName,
                               @NonNull final String dataSignature,
                               @Nullable final String developerPayload,
                               final long purchaseTime,
                               final int purchaseState,
                               final boolean autoRenewing) throws JSONException {
        super(product, VENDOR_ID, orderId, token);
        this.packageName = Check.notNull(packageName);
        this.dataSignature = Check.notNull(dataSignature);
        this.developerPayload = developerPayload;
        this.autoRenewing = autoRenewing;
        this.purchaseTime = purchaseTime;
        this.purchaseState = purchaseState;
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
}
