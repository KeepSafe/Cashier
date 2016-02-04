package com.getkeepsafe.cashier;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;
import org.json.JSONObject;

public class Purchase extends Product {
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
}
