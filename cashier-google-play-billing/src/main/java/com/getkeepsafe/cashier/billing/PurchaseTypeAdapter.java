package com.getkeepsafe.cashier.billing;

import android.os.Parcel;

import com.android.billingclient.api.Purchase;
import com.ryanharter.auto.value.parcel.TypeAdapter;

import org.json.JSONException;

public final class PurchaseTypeAdapter implements TypeAdapter<Purchase> {
    @Override
    public Purchase fromParcel(Parcel in) {
        String signature = in.readString();
        String originalJson = in.readString();
        try {
            return new Purchase(originalJson, signature);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void toParcel(Purchase value, Parcel dest) {
        dest.writeString(value.getSignature());
        dest.writeString(value.getOriginalJson());
    }
}
