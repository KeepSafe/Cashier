package com.getkeepsafe.cashier.billing;

import android.os.Parcel;
import android.os.Parcelable;

import com.getkeepsafe.cashier.CashierPurchase;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;

import org.json.JSONException;
import org.json.JSONObject;

import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE;
import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE_CANCELED;
import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE_PURCHASED;
import static com.getkeepsafe.cashier.billing.GooglePlayBillingConstants.PurchaseConstants.PURCHASE_STATE_REFUNDED;

public class GooglePlayBillingPurchase implements Parcelable, Purchase {

    protected GooglePlayBillingPurchase(Parcel in) {
        purchase = in.readParcelable(Purchase.class.getClassLoader());
        receipt = in.readString();
        token = in.readString();
        orderId = in.readString();
        purchaseState = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(purchase, flags);
        dest.writeString(receipt);
        dest.writeString(token);
        dest.writeString(orderId);
        dest.writeInt(purchaseState);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GooglePlayBillingPurchase> CREATOR = new Creator<GooglePlayBillingPurchase>() {
        @Override
        public GooglePlayBillingPurchase createFromParcel(Parcel in) {
            return new GooglePlayBillingPurchase(in);
        }

        @Override
        public GooglePlayBillingPurchase[] newArray(int size) {
            return new GooglePlayBillingPurchase[size];
        }
    };

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
                googlePlayPurchase.getDeveloperPayload(),
                googlePlayPurchase.getAccountIdentifiers().getObfuscatedAccountId());

        return new GooglePlayBillingPurchase(
                cashierPurchase,
                receipt,
                googlePlayPurchase.getPurchaseToken(),
                googlePlayPurchase.getOrderId(),
                purchaseState
        );
    }

    private final Purchase purchase;
    private final String receipt;
    private final String token;
    private final String orderId;
    private final int purchaseState;

    private GooglePlayBillingPurchase(Purchase purchase, String receipt, String token, String orderId, int purchaseState) {
        this.purchase = purchase;
        this.receipt = receipt;
        this.token = token;
        this.orderId = orderId;
        this.purchaseState = purchaseState;
    }

    public Purchase purchase() {
        return purchase;
    }

    /**
     * The original purchase data receipt from Google Play. This is useful for data signature
     * validation
     */
    public String receipt() {
        return receipt;
    }

    public String token() {
        return token;
    }

    public String orderId() {
        return orderId;
    }

    /**
     * The purchase state of the order.
     * Possible values are:
     * <ul>
     * <li>{@code 0} - Purchased</li>
     * <li>{@code 1} - Canceled</li>
     * <li>{@code 2} - Refunded</li>
     * </ul>
     */
    public int purchaseState() {
        return purchaseState;
    }

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
