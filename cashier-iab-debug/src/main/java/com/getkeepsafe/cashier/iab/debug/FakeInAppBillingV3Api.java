package com.getkeepsafe.cashier.iab.debug;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;

import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.iab.InAppBillingPurchase;
import com.getkeepsafe.cashier.iab.AbstractInAppBillingV3API;
import com.getkeepsafe.cashier.iab.InAppBillingV3Vendor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_DEVELOPER_ERROR;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_OK;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PRODUCT_TYPE_SUBSCRIPTION;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.ProductConstants;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.REQUEST_SKU_DETAILS_ITEM_LIST;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_BUY_INTENT;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_CODE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_GET_SKU_DETAILS_LIST;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_ITEM_LIST;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_PURCHASE_DATA_LIST;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_SIGNATURE_LIST;

public class FakeInAppBillingV3Api extends AbstractInAppBillingV3API {
    public static final String TEST_PRIVATE_KEY =
            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALXolIcA1LIcYDnO\n" +
            "2nfalbkOD2UAQ3KfqsdEGLddG2rW8Cyl2LIyiWVvQ6bp2q5qBoYCds9lBQT21uo1\n" +
            "VHTcv4mnaLfdBjMlzecrK8y1FzRLKFXyoMqiau8wunFeqFsdzHQ774PbYyNgMGdr\n" +
            "zUDXqIdQONL8Eq/0pgddk07uNxwbAgMBAAECgYAJInvK57zGkOw4Gu4XlK9uEomt\n" +
            "Xb0FVYVC6mV/V7qXu+FlrJJcKHOD13mDOT0VAxf+xMLomT8OR8L1EeaC087+aeza\n" +
            "twYUVx4d+J0cQ8xo3ILwY5Bg4/Y4R0gIbdKupHbhPKaLSAiMxilNKqNfY8upT2X/\n" +
            "S4OFDDbm7aK8SlGPEQJBAN+YlMb4PS54aBpWgeAP8fzgtOL0Q157bmoQyCokiWv3\n" +
            "OGa89LraifCtlsqmmAxyFbPzO2cFHYvzzEeU86XZVFkCQQDQRWQ0QJKJsfqxEeYG\n" +
            "rq9e3TkY8uQeHz8BmgxRcYC0v43bl9ggAJAzh9h9o0X9da1YzkoQ0/cWUp5NK95F\n" +
            "93WTAkEAxqm1/rcO/RwEOuqDyIXCVxF8Bm5K8UawCtNQVYlTBDeKyFW5B9AmYU6K\n" +
            "vRGZ5Oz0dYd2TwlPgEqkRTGF7eSUOQJAfyK85oC8cz2oMMsiRdYAy8Hzht1Oj2y3\n" +
            "g3zMJDNLRArix7fLgM2XOT2l1BwFL5HUPa+/2sHpxUCtzaIHz2Id7QJATyF+fzUR\n" +
            "eVw04ogIsOIdG0ECrN5/3g9pQnAjxcReQ/4KVCpIE8lQFYjAzQYUkK9VOjX9LYp9\n" +
            "DGEnpooCco1ZjA==";

    public static final String TEST_PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC16JSHANSyHGA5ztp32pW5Dg9l\n" +
            "AENyn6rHRBi3XRtq1vAspdiyMollb0Om6dquagaGAnbPZQUE9tbqNVR03L+Jp2i3\n" +
            "3QYzJc3nKyvMtRc0SyhV8qDKomrvMLpxXqhbHcx0O++D22MjYDBna81A16iHUDjS\n" +
            "/BKv9KYHXZNO7jccGwIDAQAB";

    private static final Set<Product> testProducts = new HashSet<>();
    private static final Set<InAppBillingPurchase> testPurchases = new HashSet<>();

    private final Context context;
    private final String privateKey64;

    public static void addTestProduct(Product product) {
        testProducts.add(product);
    }

    public static void addTestPurchase(InAppBillingPurchase purchase) {
        testPurchases.add(purchase);
    }

    public FakeInAppBillingV3Api(Context context) {
        this(context, TEST_PRIVATE_KEY);
    }

    public FakeInAppBillingV3Api(Context context, String privateKey64) {
        this.context = context;
        this.privateKey64 = privateKey64;
    }

    @Override
    public boolean initialize(Context context, InAppBillingV3Vendor vendor, LifecycleListener listener) {
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
    public void dispose(Context context) {}

    @Override
    public int isBillingSupported(String itemType) throws RemoteException {
        return BILLING_RESPONSE_RESULT_OK;
    }

    @Override
    public Bundle getSkuDetails(String itemType, Bundle skus)
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
                if (product.sku().equals(sku)
                        && (product.isSubscription() == (itemType.equals(PRODUCT_TYPE_SUBSCRIPTION)))) {
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
    public Bundle getBuyIntent(String sku, String itemType, String developerPayload)
            throws RemoteException {
        final Bundle bundle = new Bundle();
        Product buyMe = null;
        for (final Product product : testProducts) {
            if (sku.equals(product.sku())) {
                buyMe = product;
                break;
            }
        }

        if (buyMe == null) {
            bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE);
            return bundle;
        }

        // Can't buy thing twice
        for (final InAppBillingPurchase purchase : testPurchases) {
            if (purchase.product().sku().equals(buyMe.sku())) {
                bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED);
                return bundle;
            }
        }

        bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
        bundle.putParcelable(RESPONSE_BUY_INTENT,
                FakeInAppBillingV3CheckoutActivity.pendingIntent(
                        context, buyMe, developerPayload, privateKey64));
        return bundle;
    }

    @Override
    public Bundle getPurchases(String itemType, String paginationToken) throws RemoteException {
        final Bundle bundle = new Bundle();
        bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);

        final ArrayList<String> skus = new ArrayList<>();
        final ArrayList<String> purchaseData = new ArrayList<>();
        final ArrayList<String> dataSignatures = new ArrayList<>();

        for (final InAppBillingPurchase purchase : testPurchases) {
            final Product product = purchase.product();
            if (product.isSubscription() == itemType.equals(PRODUCT_TYPE_SUBSCRIPTION)) {
                skus.add(product.sku());
                purchaseData.add(purchase.purchaseData());
                dataSignatures.add(purchase.dataSignature());
            }
        }

        bundle.putStringArrayList(RESPONSE_INAPP_ITEM_LIST, skus);
        bundle.putStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST, purchaseData);
        bundle.putStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST, dataSignatures);

        return bundle;
    }

    @Override
    public int consumePurchase(String purchaseToken) throws RemoteException {
        for (final InAppBillingPurchase purchase : testPurchases) {
            if (purchase.token().equals(purchaseToken)) {
                testPurchases.remove(purchase);
                return BILLING_RESPONSE_RESULT_OK;
            }
        }

        return BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED;
    }

    private String productJson(Product product) throws JSONException {
        final JSONObject object = new JSONObject();
        object.put(ProductConstants.SKU, product.sku());
        object.put(ProductConstants.PRICE, product.price());
        object.put(ProductConstants.CURRENCY, product.currency());
        object.put(ProductConstants.NAME, product.name());
        object.put(ProductConstants.DESCRIPTION, product.description());
        object.put(ProductConstants.PRICE_MICRO, product.microsPrice());
        return object.toString();
    }
}
