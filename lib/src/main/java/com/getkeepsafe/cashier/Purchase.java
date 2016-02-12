package com.getkeepsafe.cashier;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;
import org.json.JSONObject;

public class Purchase extends Product implements Parcelable {
    public static final String KEY_ORDER_ID = "order-id";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_DEV_PAYLOAD = "developer-payload";

    public final String orderId;
    public final String token;
    public final String developerPayload;

    public Purchase(@NonNull final Product product,
                    @NonNull final String orderId,
                    @NonNull final String token,
                    @NonNull final String developerPayload) {
        super(product);
        this.orderId = Check.notNull(orderId, "Order ID");
        this.token = Check.notNull(token, "Token");
        this.developerPayload = Check.notNull(developerPayload, "Developer Payload");
    }

    public Purchase(@NonNull final String json) throws JSONException {
        this(new JSONObject(Check.notNull(json, "Purchase JSON")));
    }

    public Purchase(@NonNull final JSONObject json) throws JSONException {
        super(json);

        orderId = json.getString(KEY_ORDER_ID);
        token = json.getString(KEY_TOKEN);
        developerPayload = json.getString(KEY_DEV_PAYLOAD);
    }

    @NonNull
    @Override
    protected JSONObject serializeToJson() throws JSONException {
        final JSONObject object = super.serializeToJson();
        object.put(KEY_ORDER_ID, orderId);
        object.put(KEY_TOKEN, token);
        object.put(KEY_DEV_PAYLOAD, developerPayload);
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Purchase purchase = (Purchase) o;

        if (!orderId.equals(purchase.orderId)) return false;
        if (!token.equals(purchase.token)) return false;
        return developerPayload.equals(purchase.developerPayload);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + orderId.hashCode();
        result = 31 * result + token.hashCode();
        result = 31 * result + developerPayload.hashCode();
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
        dest.writeString(this.orderId);
        dest.writeString(this.token);
        dest.writeString(this.developerPayload);
    }

    protected Purchase(Parcel in) {
        super(in);
        this.orderId = in.readString();
        this.token = in.readString();
        this.developerPayload = in.readString();
    }

    public static final Creator<Purchase> CREATOR = new Creator<Purchase>() {
        public Purchase createFromParcel(Parcel source) {
            return new Purchase(source);
        }

        public Purchase[] newArray(int size) {
            return new Purchase[size];
        }
    };
}
