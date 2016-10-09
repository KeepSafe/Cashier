package com.getkeepsafe.cashier;

import com.google.auto.value.AutoValue;

import org.json.JSONException;
import org.json.JSONObject;

@AutoValue
public abstract class CashierPurchase implements Purchase {
    public static final String KEY_ORDER_ID = "order-id";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_DEV_PAYLOAD = "developer-payload";

    public abstract Product product();
    public abstract String orderId();
    public abstract String token();
    public abstract String developerPayload();

    public static CashierPurchase create(String json) throws JSONException {
        return create(new JSONObject(json));
    }

    public static CashierPurchase create(JSONObject json) throws JSONException {
        return create(Product.create(json),
                json.getString(KEY_ORDER_ID),
                json.getString(KEY_TOKEN),
                json.getString(KEY_DEV_PAYLOAD));
    }

    public static CashierPurchase create(Product product,
                                         String orderId,
                                         String token,
                                         String developerPayload) {
        return new AutoValue_CashierPurchase(product, orderId, token, developerPayload);
    }

    public JSONObject toJson() throws JSONException {
        final JSONObject object = product().toJson();
        object.put(KEY_ORDER_ID, orderId());
        object.put(KEY_TOKEN, token());
        object.put(KEY_DEV_PAYLOAD, developerPayload());
        return object;
    }
}
