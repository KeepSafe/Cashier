package com.getkeepsafe.cashier.billing;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.android.billingclient.api.Purchase;

import java.util.List;

public final class GooglePlayBillingApi extends AbstractGooglePlayBillingApi {

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public void dispose(Context context) {
    }

    @Override
    public int isBillingSupported(String itemType) {
        return 0;
    }

    @Override
    public Bundle getSkuDetails(String itemType, Bundle skus) {
        return null;
    }

    @Override
    public void launchBillingFlow(String sku, String itemType, String developerPayload) {
    }

    @Override
    public Bundle getPurchases(String itemType, String paginationToken) {
        return null;
    }

    @Override
    public int consumePurchase(String purchaseToken) {
        return 0;
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
    }
}
