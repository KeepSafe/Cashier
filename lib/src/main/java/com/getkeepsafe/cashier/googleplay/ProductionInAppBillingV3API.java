package com.getkeepsafe.cashier.googleplay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.vending.billing.IInAppBillingService;
import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedMap;
import java.util.TreeMap;

@SuppressWarnings("ConstantConditions")
public class ProductionInAppBillingV3API extends InAppBillingV3API implements GooglePlayConstants {
    @Nullable
    private IInAppBillingService billing;

    @Nullable
    private LifecycleListener listener;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            billing = IInAppBillingService.Stub.asInterface(service);
            if (listener != null) {
                listener.initialized(available());
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            billing = null;
            if (listener != null) {
                listener.disconnected();
            }
        }
    };

    @Override
    public boolean initialize(@NonNull final Context context,
                              @NonNull final InAppBillingV3Vendor vendor,
                              @Nullable final LifecycleListener listener) {
        final boolean superInited = super.initialize(context, vendor, listener);
        this.listener = listener;
        if (available()) {
            if (listener != null) {
                listener.initialized(true);
            }

            return true;
        }

        final Intent serviceIntent
                = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");

        final PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            return false;
        } else {
            final List<ResolveInfo> intentServices
                    = packageManager.queryIntentServices(serviceIntent, 0);
            if (intentServices == null || intentServices.isEmpty()) {
                return false;
            }
        }

        return superInited
                && context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean available() {
        return billing != null;
    }

    @Override
    public void dispose(@NonNull final Context context) {
        Check.notNull(context, "Context");
        try {
            context.unbindService(serviceConnection);
        } catch (IllegalArgumentException e) {
            // Never bound to begin with, ok
        }
        billing = null;
    }

    @Override
    public int isBillingSupported(@NonNull final String itemType) throws RemoteException {
        throwIfUnavailable();
        return billing.isBillingSupported(API_VERSION, packageName, itemType);
    }

    @Override
    public Bundle getSkuDetails(@NonNull final String itemType,
                                @NonNull final Bundle skus) throws RemoteException {
        throwIfUnavailable();

        // The workaround below only applies to inapp products as there are no static testing
        // subscription products
        if (itemType.equals(PRODUCT_TYPE_SUBSCRIPTION)) {
            return billing.getSkuDetails(API_VERSION, packageName, itemType, skus);
        }

        // Workaround for static testing bug:
        // #getSkuDetails does not return any details for any static test products, so we
        // need to extract those SKUs, if they exist, and provide details separately, then
        // re-join with the API result
        final ArrayList<String> requestedSkus
                = skus.getStringArrayList(REQUEST_SKU_DETAILS_ITEM_LIST);
        final SortedMap<Integer, String> joinSkus = new TreeMap<>();
        if (requestedSkus != null) {
            final ListIterator<String> it = requestedSkus.listIterator();
            while (it.hasNext()) {
                final int index = it.nextIndex();
                final String sku = it.next();
                if (sku.equals(TEST_PRODUCT_PURCHASED)
                        || sku.equals(TEST_PRODUCT_CANCELED)
                        || sku.equals(TEST_PRODUCT_REFUNDED)) {
                    joinSkus.put(index, sku);
                    it.remove();
                }
            }
        }

        if (!joinSkus.isEmpty()) {
            skus.putStringArrayList(REQUEST_SKU_DETAILS_ITEM_LIST, requestedSkus);
        }

        final Bundle result;
        if (!requestedSkus.isEmpty()) {
            result = billing.getSkuDetails(API_VERSION, packageName, itemType, skus);
        } else {
            result = new Bundle();
            result.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
            result.putStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST, new ArrayList<String>());
        }

        if (!joinSkus.isEmpty()) {
            ArrayList<String> detailsList = result.getStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST);
            if (detailsList == null) {
                detailsList = new ArrayList<>(joinSkus.size());
                for (final Integer key : joinSkus.keySet()) {
                    try {
                        detailsList.add(testJsonOf(joinSkus.get(key)));
                    } catch (JSONException e) {
                        // This is a library error, promote to RuntimeException
                        throw new RuntimeException(e);
                    }
                }
            } else {
                for (final Integer key : joinSkus.keySet()) {
                    try {
                        detailsList.add(key, testJsonOf(joinSkus.get(key)));
                    } catch (JSONException e) {
                        // This is a library error, promote to RuntimeException
                        throw new RuntimeException(e);
                    }
                }
            }

            result.putStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST, detailsList);
        }

        return result;
    }

    private String testJsonOf(@NonNull final String sku) throws JSONException {
        Check.notNull(sku, "SKU");
        final JSONObject object = new JSONObject();
        object.put(ProductConstants.SKU, sku);
        object.put(ProductConstants.PRICE, "$0.99");
        object.put(ProductConstants.CURRENCY, "USD");
        object.put(ProductConstants.NAME, "Test Product: " + sku);
        object.put(ProductConstants.DESCRIPTION, "Test Product with SKU: " + sku);
        object.put(ProductConstants.PRICE_MICRO, 990_000L);
        return object.toString();
    }

    @Override
    public Bundle getBuyIntent(@NonNull final String sku,
                               @NonNull final String itemType,
                               @Nullable final String developerPayload) throws RemoteException {
        throwIfUnavailable();
        return billing.getBuyIntent(API_VERSION, packageName, sku, itemType, developerPayload);
    }

    @Override
    public Bundle getPurchases(@NonNull final String itemType,
                               @Nullable final String paginationToken) throws RemoteException {
        throwIfUnavailable();
        return billing.getPurchases(API_VERSION, packageName, itemType, paginationToken);
    }

    @Override
    public int consumePurchase(@NonNull final String purchaseToken) throws RemoteException {
        throwIfUnavailable();
        return billing.consumePurchase(API_VERSION, packageName, purchaseToken);
    }

    @Override
    protected void throwIfUnavailable() {
        super.throwIfUnavailable();
        if (!available()) {
            throw new IllegalStateException("Trying to use API when unavailable");
        }
    }
}
