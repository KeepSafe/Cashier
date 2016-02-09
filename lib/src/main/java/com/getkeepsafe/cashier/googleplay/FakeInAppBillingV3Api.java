package com.getkeepsafe.cashier.googleplay;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FakeInAppBillingV3Api extends InAppBillingV3API implements GooglePlayConstants {
    public static final Set<Product> testProducts = new HashSet<>();
    public static final Set<GooglePlayPurchase> testPurchases = new HashSet<>();

    private final Context context;

    public FakeInAppBillingV3Api(@NonNull final Context context) {
        this.context = Check.notNull(context, "Context");
    }

    @Override
    public boolean initialize(@NonNull final Context context,
                              @NonNull final InAppBillingV3Vendor vendor,
                              @Nullable final LifecycleListener listener) {
        super.initialize(context, vendor, listener);

        if (available()) {
            if (listener != null) {
                listener.initialized(true);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public void dispose(@NonNull final Context context) {}

    @Override
    public int isBillingSupported(@NonNull final String itemType) throws RemoteException {
        return BILLING_RESPONSE_RESULT_OK;
    }

    @Override
    public Bundle getSkuDetails(@NonNull final String itemType, @NonNull final Bundle skus)
            throws RemoteException {
        final Bundle bundle = new Bundle();
        final ArrayList<String> skuList = skus.getStringArrayList(REQUEST_SKU_DETAILS_ITEM_LIST);
        final ArrayList<String> resultList = new ArrayList<>();
        if (skuList == null || skuList.size() > 20) {
            bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_DEVELOPER_ERROR);
            return bundle;
        }

        for (final String sku : skuList) {
            for (final Product product : testProducts) {
                if (product.sku.equals(sku)
                        && (product.isSubscription == (itemType.equals(PRODUCT_TYPE_SUBSCRIPTION)))) {
                    try {
                        resultList.add(productJson(product));
                    } catch (JSONException e) {
                        // This is a library error, promote to RuntimeException
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
        }

        bundle.putStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST, resultList);

        if (resultList.size() != skuList.size()) {
            bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_DEVELOPER_ERROR);
        } else {
            bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
        }

        return bundle;
    }

    @Override
    public Bundle getBuyIntent(@NonNull final String sku,
                               @NonNull final String itemType,
                               @Nullable final String developerPayload) throws RemoteException {
        final Bundle bundle = new Bundle();
        Product buyMe = null;
        for (final Product product : testProducts) {
            if (sku.equals(product.sku)) {
                buyMe = product;
                break;
            }
        }

        if (buyMe == null) {
            bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE);
            return bundle;
        }

        // Can't buy thing twice
        for (final GooglePlayPurchase purchase : testPurchases) {
            if (purchase.sku.equals(buyMe.sku)) {
                bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED);
                return bundle;
            }
        }

        bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
        bundle.putParcelable(RESPONSE_BUY_INTENT,
                FakeInAppBillingV3CheckoutActivity.pendingIntent(context, buyMe, developerPayload));
        return bundle;
    }

    @Override
    public Bundle getPurchases(@NonNull final String itemType,
                               @Nullable final String paginationToken) throws RemoteException {
        final Bundle bundle = new Bundle();
        bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);

        final ArrayList<String> skus = new ArrayList<>();
        final ArrayList<String> purchaseData = new ArrayList<>();
        final ArrayList<String> dataSignatures = new ArrayList<>();

        for (final GooglePlayPurchase purchase : testPurchases) {
            if (purchase.isSubscription == (itemType.equals(PRODUCT_TYPE_SUBSCRIPTION))) {
                try {
                    skus.add(purchase.sku);
                    purchaseData.add(purchaseJson(purchase));
                    dataSignatures.add("TEST-DATA-SIGNATURE-" + purchase.sku);
                } catch (JSONException e) {
                    // This is a library error, promote to RuntimeException
                    throw new RuntimeException(e);
                }
            }
        }

        bundle.putStringArrayList(RESPONSE_INAPP_ITEM_LIST, skus);
        bundle.putStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST, purchaseData);
        bundle.putStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST, dataSignatures);

        return bundle;
    }

    @Override
    public int consumePurchase(@NonNull final String purchaseToken) throws RemoteException {
        for (final GooglePlayPurchase purchase : testPurchases) {
            if (purchase.token.equals(purchaseToken)) {
                testPurchases.remove(purchase);
                return BILLING_RESPONSE_RESULT_OK;
            }
        }

        return BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED;
    }

    private String purchaseJson(@NonNull final GooglePlayPurchase purchase) throws JSONException {
        final JSONObject object = new JSONObject();
        object.put(PurchaseConstants.AUTO_RENEWING, purchase.autoRenewing);
        object.put(PurchaseConstants.ORDER_ID, purchase.orderId);
        object.put(PurchaseConstants.PACKAGE_NAME, purchase.packageName);
        object.put(PurchaseConstants.PRODUCT_ID, purchase.sku);
        object.put(PurchaseConstants.PURCHASE_TIME, purchase.purchaseTime);
        object.put(PurchaseConstants.PURCHASE_STATE, purchase.purchaseState);
        object.put(PurchaseConstants.DEVELOPER_PAYLOAD, purchase.developerPayload);
        object.put(PurchaseConstants.PURCHASE_TOKEN, purchase.token);
        return object.toString();

    }

    private String productJson(@NonNull final Product product) throws JSONException {
        final JSONObject object = new JSONObject();
        object.put(ProductConstants.SKU, product.sku);
        object.put(ProductConstants.PRICE, product.price);
        object.put(ProductConstants.CURRENCY, product.currency);
        object.put(ProductConstants.NAME, product.name);
        object.put(ProductConstants.DESCRIPTION, product.description);
        object.put(ProductConstants.PRICE_MICRO, product.microsPrice);
        return object.toString();
    }
}
