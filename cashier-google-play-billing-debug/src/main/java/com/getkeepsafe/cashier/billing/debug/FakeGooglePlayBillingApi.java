package com.getkeepsafe.cashier.billing.debug;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.billing.AbstractGooglePlayBillingApi;
import com.getkeepsafe.cashier.billing.GooglePlayBillingVendor;
import com.getkeepsafe.cashier.logging.Logger;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FakeGooglePlayBillingApi extends AbstractGooglePlayBillingApi {

    @VisibleForTesting
    static final String TEST_PRIVATE_KEY =
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

    /**
     * {@link com.getkeepsafe.cashier.billing.debug.FakeGooglePlayBillingApi} is using predefined
     * private key to sign purchase receipt. Use this matching public key if you want to verify
     * signature in your code.
     */
    public static final String TEST_PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC16JSHANSyHGA5ztp32pW5Dg9l\n" +
                    "AENyn6rHRBi3XRtq1vAspdiyMollb0Om6dquagaGAnbPZQUE9tbqNVR03L+Jp2i3\n" +
                    "3QYzJc3nKyvMtRc0SyhV8qDKomrvMLpxXqhbHcx0O++D22MjYDBna81A16iHUDjS\n" +
                    "/BKv9KYHXZNO7jccGwIDAQAB";

    private static final Set<Product> testProducts = new HashSet<>();
    private static final Set<Purchase> testInappPurchases = new HashSet<>();
    private static final Set<Purchase> testSubPurchases = new HashSet<>();

    private static final Map<String, FakePurchaseListener> pendingPurchases = new HashMap<>();

    private GooglePlayBillingVendor vendor;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public FakeGooglePlayBillingApi(Context context) {
        this(context, TEST_PRIVATE_KEY);
    }

    public FakeGooglePlayBillingApi(Context context, String privateKey64) {
    }

    public static void addTestProduct(Product product) {
        testProducts.add(product);
    }

    /**
     * Notifies pending purchase listeners of successful transaction
     * @param sku Sku of purchased product
     * @param purchase Purchase object representing successful transaction
     */
    static void notifyPurchaseSuccess(String sku, Purchase purchase) {
        FakePurchaseListener listener = pendingPurchases.get(sku);
        if (listener != null) {
            listener.onFakePurchaseSuccess(purchase);
        }
    }

    /**
     * Notifies pending purchase listeners of transation error
     * @param sku Sku of purchased product
     * @param responseCode Error code
     */
    static void notifyPurchaseError(String sku, int responseCode) {
        FakePurchaseListener listener = pendingPurchases.get(sku);
        if (listener != null) {
            listener.onFakePurchaseError(responseCode);
        }
    }

    @Override
    public boolean initialize(@NonNull Context context, @NonNull GooglePlayBillingVendor vendor, LifecycleListener listener, Logger logger) {
        super.initialize(context, vendor, listener, logger);
        this.vendor = vendor;
        listener.initialized(true);
        return true;
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public void dispose() {
    }

    @Override
    public int isBillingSupported(String itemType) {
        return BillingClient.BillingResponse.OK;
    }

    @Override
    public void launchBillingFlow(@NonNull Activity activity, @NonNull final String sku, final String itemType) {
        for (Product product : testProducts) {
            if (product.sku().equals(sku)) {
                activity.startActivity(FakeGooglePlayCheckoutActivity.intent(activity, product, TEST_PRIVATE_KEY));

                // Put listener to pendingPurchases map and wait until either
                // notifyPurchaseSuccess or notifyPurchaseError is called from FakeGooglePlayCheckoutActivity
                pendingPurchases.put(sku, new FakePurchaseListener() {
                    @Override
                    public void onFakePurchaseSuccess(Purchase purchase) {
                        pendingPurchases.remove(sku);
                        if (itemType.equals(BillingClient.SkuType.SUBS)) {
                            testSubPurchases.add(purchase);
                        } else {
                            testInappPurchases.add(purchase);
                        }
                        vendor.onPurchasesUpdated(BillingClient.BillingResponse.OK, Collections.singletonList(purchase));
                    }

                    @Override
                    public void onFakePurchaseError(int responseCode) {
                        pendingPurchases.remove(sku);
                        vendor.onPurchasesUpdated(responseCode, null);
                    }
                });
                return;
            }
        }
    }

    @Nullable
    @Override
    public List<Purchase> getPurchases() {
        ArrayList<Purchase> purchases = new ArrayList<>();
        purchases.addAll(testInappPurchases);
        purchases.addAll(testSubPurchases);
        return purchases;
    }

    @Nullable
    @Override
    public List<Purchase> getPurchases(String itemType) {
        if (itemType.equals(BillingClient.SkuType.SUBS)) {
            return new ArrayList<>(testSubPurchases);
        } else {
            return new ArrayList<>(testInappPurchases);
        }
    }

    @Override
    public void consumePurchase(final @NonNull String purchaseToken, final @NonNull ConsumeResponseListener listener) {
        // Use new thread to simulate network operation
        new Thread() {
            public void run() {
                // Wait 1 second to simulate network operation
                try { sleep(1000L); } catch (InterruptedException e) {}

                for (Iterator<Purchase> it = testInappPurchases.iterator(); it.hasNext();) {
                    if (it.next().getPurchaseToken().equals(purchaseToken)) {
                        it.remove();
                    }
                }
                for (Iterator<Purchase> it = testSubPurchases.iterator(); it.hasNext();) {
                    if (it.next().getPurchaseToken().equals(purchaseToken)) {
                        it.remove();
                    }
                }

                // Return result on main thread
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onConsumeResponse(BillingClient.BillingResponse.OK, purchaseToken);
                    }
                });
            }
        }.start();
    }

    @Override
    public void getSkuDetails(final String itemType, final @NonNull List<String> skus, final @NonNull SkuDetailsResponseListener listener) {
        // Use new thread to simulate network operation
        new Thread() {
            public void run() {
                // Wait 1 second to simulate network operation
                try { sleep(1000L); } catch (InterruptedException e) {}

                final List<SkuDetails> details = new ArrayList<>();
                for (Product product : testProducts) {
                    if (skus.contains(product.sku())) {
                        try {
                            details.add(new FakeSkuDetails(product));
                        } catch (JSONException e) {
                        }
                    }
                }

                // Return result on main thread
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onSkuDetailsResponse(BillingClient.BillingResponse.OK, details);
                    }
                });
            }
        }.start();
    }

    public static interface FakePurchaseListener {
        void onFakePurchaseSuccess(Purchase purchase);
        void onFakePurchaseError(int responseCode);
    }
}
