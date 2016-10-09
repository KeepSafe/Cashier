package com.getkeepsafe.cashier.googleplay;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;



import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FakeInAppBillingV3Api extends InAppBillingV3API implements GooglePlayConstants {
    private static final Set<Product> testProducts = new HashSet<>();
    private static final Set<GooglePlayPurchase> testPurchases = new HashSet<>();

    private final Context context;

    public static void addTestProduct(final Product product) {
        testProducts.add(product);
    }

    public static void addTestPurchase(final GooglePlayPurchase purchase) {
        testPurchases.add(purchase);
    }

    public FakeInAppBillingV3Api(final Context context) {
        this.context = Check.notNull(context, "Context");
    }

    @Override
    public boolean initialize(final Context context,
                              final InAppBillingV3Vendor vendor,
                              final LifecycleListener listener) {
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
    public void dispose(final Context context) {}

    @Override
    public int isBillingSupported(final String itemType) throws RemoteException {
        return BILLING_RESPONSE_RESULT_OK;
    }

    @Override
    public Bundle getSkuDetails(final String itemType, final Bundle skus)
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
    public Bundle getBuyIntent(final String sku,
                               final String itemType,
                               final String developerPayload) throws RemoteException {
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
    public Bundle getPurchases(final String itemType,
                               final String paginationToken) throws RemoteException {
        final Bundle bundle = new Bundle();
        bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);

        final ArrayList<String> skus = new ArrayList<>();
        final ArrayList<String> purchaseData = new ArrayList<>();
        final ArrayList<String> dataSignatures = new ArrayList<>();

        for (final GooglePlayPurchase purchase : testPurchases) {
            if (purchase.isSubscription == (itemType.equals(PRODUCT_TYPE_SUBSCRIPTION))) {
                skus.add(purchase.sku);
                purchaseData.add(purchase.purchaseData);
                dataSignatures.add("TEST-DATA-SIGNATURE-" + purchase.sku);
            }
        }

        bundle.putStringArrayList(RESPONSE_INAPP_ITEM_LIST, skus);
        bundle.putStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST, purchaseData);
        bundle.putStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST, dataSignatures);

        return bundle;
    }

    @Override
    public int consumePurchase(final String purchaseToken) throws RemoteException {
        for (final GooglePlayPurchase purchase : testPurchases) {
            if (purchase.token.equals(purchaseToken)) {
                testPurchases.remove(purchase);
                return BILLING_RESPONSE_RESULT_OK;
            }
        }

        return BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED;
    }

    private String productJson(final Product product) throws JSONException {
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
