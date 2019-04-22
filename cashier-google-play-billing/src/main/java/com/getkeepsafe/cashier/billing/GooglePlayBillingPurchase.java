package com.getkeepsafe.cashier.billing;

import android.os.Parcelable;

import com.getkeepsafe.cashier.CashierPurchase;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;
import com.google.auto.value.AutoValue;

import org.json.JSONException;
import org.json.JSONObject;

import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE;
import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE_CANCELED;
import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE_PURCHASED;
import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE_REFUNDED;

@AutoValue
public abstract class GooglePlayBillingPurchase implements Parcelable, Purchase {

    public static GooglePlayBillingPurchase create(Product product,
                                                   com.android.billingclient.api.Purchase googlePlayPurchase)
            throws JSONException {
        final String receipt = googlePlayPurchase.getOriginalJson();
        final JSONObject jsonPurchase = new JSONObject(receipt);
        final int purchaseState = jsonPurchase.getInt(PURCHASE_STATE);
        final CashierPurchase cashierPurchase = CashierPurchase.create(product,
                googlePlayPurchase.getOrderId(),
                googlePlayPurchase.getPurchaseToken(),
                googlePlayPurchase.getOriginalJson(),
                // NOTE: Developer payload is not supported with Google Play Billing
                // https://issuetracker.google.com/issues/63381481
                "");

        return new AutoValue_GooglePlayBillingPurchase(cashierPurchase, receipt, googlePlayPurchase.getPurchaseToken(), googlePlayPurchase.getOrderId(), purchaseState);
    }

    public abstract Purchase purchase();

    /**
     * The original purchase data receipt from Google Play. This is useful for data signature
     * validation
     */
    public abstract String receipt();

    public abstract String token();

    public abstract String orderId();

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

    public Product product() {
        return purchase().product();
    }

    public String developerPayload() {
        throw new RuntimeException("Developer payload is not supported in Google Play Billing!");
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
        return new JSONObject(receipt());
    }
}
