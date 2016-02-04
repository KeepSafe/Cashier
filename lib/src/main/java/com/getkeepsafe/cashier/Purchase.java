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
    public static final String KEY_EXTRAS = "extras";

    public final String orderId;
    public final String token;
    public final String developerPayload;

    private String extras;

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
        extras = json.optString(KEY_EXTRAS);
    }

    @Nullable
    public String getExtras() {
         return extras;
    }

    public void setExtras(final String extras) {
        this.extras = extras;
    }

    @NonNull
    @Override
    protected JSONObject serializeToJson() throws JSONException {
        final JSONObject object = super.serializeToJson();
        object.put(KEY_ORDER_ID, orderId);
        object.put(KEY_TOKEN, token);
        object.put(KEY_DEV_PAYLOAD, developerPayload);
        object.put(KEY_EXTRAS, extras);
        return object;
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
        dest.writeString(this.extras);
    }

    protected Purchase(Parcel in) {
        super(in);
        this.orderId = in.readString();
        this.token = in.readString();
        this.developerPayload = in.readString();
        this.extras = in.readString();
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
