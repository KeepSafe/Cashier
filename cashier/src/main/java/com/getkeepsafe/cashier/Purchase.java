package com.getkeepsafe.cashier;

import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public interface Purchase extends Parcelable {
    Product product();
    String orderId();
    String token();
    String developerPayload();
    String receipt();
    JSONObject toJson() throws JSONException;
}
