package com.getkeepsafe.cashier.billing;

import android.os.Parcelable;

import com.getkeepsafe.cashier.CashierPurchase;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;
import com.google.auto.value.AutoValue;
import com.ryanharter.auto.value.parcel.ParcelAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.PurchaseConstants.DEVELOPER_PAYLOAD;
import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE_CANCELED;
import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE_PURCHASED;
import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE_REFUNDED;

@AutoValue
public abstract class GooglePlayBillingPurchase implements Parcelable, Purchase {

    public static final String GP_KEY_DATA_SIG = "gp-data-signature";
    public static final String GP_KEY_PURCHASE_STATE = "purchaseState";
    public static final String GP_KEY_PURCHASE_DATA = "gp-purchase-data";

    public static GooglePlayBillingPurchase create(com.android.billingclient.api.Purchase googlePlayPurchase)
            throws JSONException {
        // TODO: Pass product as an object here
        final JSONObject jsonPurchase = new JSONObject(googlePlayPurchase.getOriginalJson());
        final Product product = Product.create(googlePlayPurchase.getOriginalJson());
        final int purchaseState = jsonPurchase.getInt(GP_KEY_PURCHASE_STATE);
        final String receipt = jsonPurchase.getString(GP_KEY_PURCHASE_DATA);
        final String developerPayload = jsonPurchase.optString(DEVELOPER_PAYLOAD, "");
        final CashierPurchase cashierPurchase = CashierPurchase.create(product,
                googlePlayPurchase.getOrderId(),
                googlePlayPurchase.getPurchaseToken(),
                receipt, developerPayload);

        return new AutoValue_GooglePlayBillingPurchase(googlePlayPurchase, cashierPurchase, purchaseState, receipt);
    }

    public static GooglePlayBillingPurchase create(JSONObject json) throws JSONException {
        // TODO: Figure out how to create a Google Play Billing Purchase from Json Object
        final com.android.billingclient.api.Purchase googlePlayPurchase =
                new com.android.billingclient.api.Purchase(json.toString(), json.getString(GP_KEY_DATA_SIG));
        final Product product = Product.create(googlePlayPurchase.getOriginalJson());
        final int purchaseState = json.getInt(GP_KEY_PURCHASE_STATE);
        final String receipt = json.getString(GP_KEY_PURCHASE_DATA);
        final String developerPayload = json.optString(DEVELOPER_PAYLOAD, "");
        final CashierPurchase cashierPurchase = CashierPurchase.create(product,
                googlePlayPurchase.getOrderId(),
                googlePlayPurchase.getPurchaseToken(),
                receipt, developerPayload);

        return new AutoValue_GooglePlayBillingPurchase(googlePlayPurchase, cashierPurchase, purchaseState, receipt);
    }


    @ParcelAdapter(PurchaseTypeAdapter.class)
    public abstract com.android.billingclient.api.Purchase googlePlayPurchase();

    public abstract Purchase purchase();

    /**
     * The purchase state of the order.
     * Possible values are:
     * <ul>
     * <li>{@code 0} - Purchased</li>
     * <li>{@code 1} - Canceled</li>
     * <li>{@code 2} - Refunded</li>
     * </ul>
     */
    public abstract int purchaseState();

    /**
     * The original purchase data receipt from Google Play. This is useful for data signature
     * validation
     */
    public abstract String receipt();

    public String packageName() {
        return googlePlayPurchase().getPackageName();
    }

    public String dataSignature() {
        return googlePlayPurchase().getSignature();
    }

    public long purchaseTime() {
        return googlePlayPurchase().getPurchaseTime();
    }

    public Product product() {
        return purchase().product();
    }

    public boolean autoRenewing() {
        return googlePlayPurchase().isAutoRenewing();
    }

    public String orderId() {
        return googlePlayPurchase().getOrderId();
    }

    public String token() {
        return googlePlayPurchase().getPurchaseToken();
    }

    public String developerPayload() {
        return purchase().developerPayload();
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
        return new JSONObject(googlePlayPurchase().getOriginalJson());
    }
}
