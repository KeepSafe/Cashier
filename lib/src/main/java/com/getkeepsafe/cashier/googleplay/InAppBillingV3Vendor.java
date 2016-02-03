package com.getkeepsafe.cashier.googleplay;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.getkeepsafe.cashier.ConsumeListener;
import com.getkeepsafe.cashier.Inventory;
import com.getkeepsafe.cashier.InventoryListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.PurchaseListener;
import com.getkeepsafe.cashier.Vendor;
import com.getkeepsafe.cashier.logging.Logger;
import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class InAppBillingV3Vendor implements Vendor, GooglePlayConstants {
    private static final String PRODUCT_TYPE_ITEM = "inapp";
    private static final String PRODUCT_TYPE_SUBSCRIPTION = "subs";
    private static final int API_VERSION = 3;

    private final String packageName;

    @Nullable
    private IInAppBillingService inAppBillingService;
    @Nullable
    private Logger logger;
    @Nullable
    private String developerPayload;
    private InitializationListener initializationListener;
    private Product pendingProduct;
    private PurchaseListener purchaseListener;

    private boolean canPurchaseItems;
    private boolean canSubscribe;
    private boolean initialized;
    private boolean available;
    private int requestCode;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            inAppBillingService = IInAppBillingService.Stub.asInterface(service);
            if (inAppBillingService == null) {
                logAndDisable("Couldn't create InAppBillingService instance");
                return;
            }

            try {
                canPurchaseItems = inAppBillingService
                        .isBillingSupported(API_VERSION, packageName, PRODUCT_TYPE_ITEM)
                            == BILLING_RESPONSE_RESULT_OK;

                canSubscribe = inAppBillingService
                        .isBillingSupported(API_VERSION, packageName, PRODUCT_TYPE_SUBSCRIPTION)
                            == BILLING_RESPONSE_RESULT_OK;
                available = canPurchaseItems || canSubscribe;
                log("Connected to service and it is " + (available ? "available" : "not available"));
                initializationListener.initialized();
            } catch (RemoteException e) {
                logAndDisable(Log.getStackTraceString(e));
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            logAndDisable("Disconnected from service");
        }
    };

    public InAppBillingV3Vendor(@NonNull final String packageName) {
        this(packageName, null);
    }

    public InAppBillingV3Vendor(@NonNull final String packageName,
                                @Nullable final String developerPayload) {
        this.packageName = Check.notNull(packageName, "Package Name");
        this.developerPayload = developerPayload;
        available = false;
        initialized = false;
    }

    @Override
    public void initialize(@NonNull final Activity activity,
                           @NonNull final InitializationListener listener) {
        Check.notNull(activity, "Activity");
        Check.notNull(listener, "Initialization Listener");
        initializationListener = listener;

        if (initialized) {
            initializationListener.initialized();
            return;
        }

        log("Initializing In-app billing v3...");

        final Intent serviceIntent
                = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");

        final PackageManager packageManager = activity.getPackageManager();
        if (packageManager == null) {
            logAndDisable("No package manager received");
            return;
        } else {
            final List<ResolveInfo> intentServices
                    = packageManager.queryIntentServices(serviceIntent, 0);
            if (intentServices == null || intentServices.isEmpty()) {
                logAndDisable("No service to receive the intent");
                return;
            }
        }

        try {
            available = activity
                    .bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            initialized = true;
        } catch (SecurityException e) {
            logAndDisable("Your app does not have the billing permission!");
        }
    }

    @Override
    public void dispose(@NonNull final Activity activity) {
        log("Disposing self...");
        Check.notNull(activity, "Activity");
        if (inAppBillingService != null) {
            activity.unbindService(serviceConnection);
        }
    }

    @Override
    public boolean available() {
        return initialized && available && canPurchaseAnything();
    }

    @Override
    public boolean canPurchase(@NonNull final Product product) {
        Check.notNull(product, "Product");
        if (!canPurchaseAnything()) {
            return false;
        }

        if (product.isSubscription && !canSubscribe) {
            return false;
        }

        if (!product.isSubscription && !canPurchaseItems) {
            return false;
        }

        // TODO: Maybe query inventory and match skus

        return true;
    }

    @Override
    public void purchase(@NonNull final Activity activity,
                            @NonNull final Product product,
                            @NonNull final PurchaseListener listener) {
        Check.notNull(activity, "Activity");
        Check.notNull(product, "Product");
        Check.notNull(listener, "Purchase Listener");
        throwIfUninitialized();

        if (!canPurchase(product)) {
            throw new IllegalArgumentException("Cannot purchase given product!" + product.toString());
        }

        log("Constructing buy intent...");
        final String type = product.isSubscription ? PRODUCT_TYPE_SUBSCRIPTION : PRODUCT_TYPE_ITEM;
        try {
            //noinspection ConstantConditions
            final Bundle buyBundle = inAppBillingService
                    .getBuyIntent(API_VERSION, packageName, product.sku, type, developerPayload);

            final int response = getResponseCode(buyBundle);
            if (response != BILLING_RESPONSE_RESULT_OK) {
                log("Couldn't purchase product! code:" + response);
                listener.failure(product, purchaseCode(response));
                return;
            }

            final PendingIntent pendingIntent = buyBundle.getParcelable(RESPONSE_BUY_INTENT);
            if (pendingIntent == null) {
                log("Received no pending intent!");
                listener.failure(product, purchaseCode(response));
                return;
            }

            log("Launching buy intent for " + product.sku);
            this.purchaseListener = listener;
            pendingProduct = product;
            requestCode = new Random().nextInt(1024);
            activity.startIntentSenderForResult(pendingIntent.getIntentSender(),
                    requestCode,
                    new Intent(), 0, 0, 0);
        } catch (RemoteException | IntentSender.SendIntentException e) {
            log("Failed to launch purchase!\n" + Log.getStackTraceString(e));
            listener.failure(product, purchaseCode(BILLING_RESPONSE_RESULT_ERROR));
        }
    }

    @Override
    public void consume(@NonNull final Activity activity,
                        @NonNull final Purchase purchase,
                        @NonNull final ConsumeListener listener) {
        Check.notNull(activity, "Activity");
        Check.notNull(purchase, "Purchase");
        Check.notNull(listener, "Consume Listener");
        throwIfUninitialized();

        if (purchase.isSubscription) {
            throw new IllegalArgumentException("Cannot consume a subscription!");
        }

        try {
            log("Consuming " + purchase.sku + " " + purchase.token);
            //noinspection ConstantConditions
            final int response
                    = inAppBillingService.consumePurchase(API_VERSION, packageName, purchase.token);
            if (response == BILLING_RESPONSE_RESULT_OK) {
                log("Successfully consumed purchase!");
                listener.success(purchase);
            } else {
                log("Couldn't consume purchase! " + response);
                listener.failure(purchase, consumeCode(response));
            }
        } catch (RemoteException e) {
            log("Couldn't consume purchase! " + Log.getStackTraceString(e));
            listener.failure(purchase, consumeCode(BILLING_RESPONSE_RESULT_ERROR));
        }
    }

    @Override
    public void getInventory(@NonNull final Activity activity,
                             @NonNull final InventoryListener listener) {
        Check.notNull(activity, "Activity");
        Check.notNull(listener, "Inventory Listener");
        throwIfUninitialized();

        final Inventory inventory = new Inventory();
        try {
            log("Querying inventory...");
            inventory.addPurchases(getPurchases(PRODUCT_TYPE_ITEM));
            inventory.addPurchases(getPurchases(PRODUCT_TYPE_SUBSCRIPTION));
            listener.success(inventory);
        } catch (RemoteException | ApiException e) {
            listener.failure(INVENTORY_QUERY_FAILURE);
        } catch (JSONException e) {
            listener.failure(INVENTORY_QUERY_MALFORMED_RESPONSE);
        }
    }

    private List<GooglePlayPurchase> getPurchases(@NonNull final String type)
            throws RemoteException, ApiException, JSONException {
        throwIfUninitialized();
        if (type.equals(PRODUCT_TYPE_ITEM)) {
            log("Querying item purchases...");
        } else {
            log("Querying subscription purchases...");
        }
        String paginationToken = null;
        final List<GooglePlayPurchase> purchaseList = new ArrayList<>();
        do {
            //noinspection ConstantConditions
            final Bundle purchases
                    = inAppBillingService.getPurchases(API_VERSION, packageName, type, paginationToken);

            final int response = getResponseCode(purchases);
            log("Got response: " + response);
            if (response != BILLING_RESPONSE_RESULT_OK) {
                throw new ApiException(response);
            }

            if (!purchases.containsKey(RESPONSE_INAPP_ITEM_LIST)
                    || !purchases.containsKey(RESPONSE_INAPP_PURCHASE_DATA_LIST)
                    || !purchases.containsKey(RESPONSE_INAPP_SIGNATURE_LIST)) {
                throw new ApiException(BILLING_RESPONSE_RESULT_ERROR);
            }

            final List<String> purchasedSkus
                    = purchases.getStringArrayList(RESPONSE_INAPP_ITEM_LIST);
            final List<String> purchaseDataList
                    = purchases.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST);
            final List<String> signatureList
                    = purchases.getStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST);

            if (purchasedSkus == null || purchaseDataList == null || signatureList == null) {
                return Collections.emptyList();
            }



            for (int i = 0; i < purchaseDataList.size(); ++i) {
                final String purchaseData = purchaseDataList.get(i);
                final String sku = purchasedSkus.get(i);
                final String signature = signatureList.get(i);

                log("Found purchase: " + sku);

                // TODO: Security verification?
                final Product product;
                if (type.equals(PRODUCT_TYPE_ITEM)) {
                    product = Product.item(sku);
                } else {
                    product = Product.subscription(sku);
                }

                purchaseList.add(GooglePlayPurchase.of(product, purchaseData, signature));
            }

            paginationToken = purchases.getString(INAPP_CONTINUATION_TOKEN);
            if (paginationToken != null) {
                log("Pagination token found, continuing on....");
            }
        } while (!TextUtils.isEmpty(paginationToken));

        return Collections.unmodifiableList(purchaseList);
    }

    private List<Product> getProducts(@NonNull final List<String> skus) {
        throwIfUninitialized();
        Check.notNull(skus, "SKU list");
        log("Retrieving sku details for " + skus.size() + " skus");

        for (int i = 0; i < skus.size(); ++i) {
            
        }
    }

    @Override
    public void setLogger(@Nullable final Logger logger) {
        this.logger = logger;
        if (this.logger != null) {
            this.logger.setTag("InAppBillingV3");
        }
    }

    @Override
    public boolean onActivityResult(final int requestCode,
                                    final int resultCode,
                                    final Intent data) {
        log("onActivityResult " + resultCode);
        if (this.requestCode != requestCode) {
            return false;
        }

        if (data == null) {
            purchaseListener.failure(pendingProduct, purchaseCode(BILLING_RESPONSE_RESULT_ERROR));
            return true;
        }

        final int responseCode = getResponseCode(data);
        if (resultCode == Activity.RESULT_OK && responseCode == BILLING_RESPONSE_RESULT_OK) {
            log("Successful purchase of " + pendingProduct.sku + "!");

            try {
                purchaseListener.success(GooglePlayPurchase.of(pendingProduct, data));
            } catch (JSONException e) {
                purchaseListener.failure(pendingProduct, Vendor.PURCHASE_SUCCESS_RESULT_MALFORMED);
            }
        } else if (resultCode == Activity.RESULT_OK) {
            log("Purchase failed! " + responseCode);
            purchaseListener.failure(pendingProduct, purchaseCode(responseCode));
        } else {
            log("Purchase canceled! " + responseCode);
            purchaseListener.failure(pendingProduct, purchaseCode(responseCode));
        }

        return true;
    }

    private void throwIfUninitialized() {
        if (inAppBillingService == null || !initialized) {
            throw new IllegalStateException("Trying to purchase without initializing first!");
        }
    }

    private boolean canPurchaseAnything() {
        return canPurchaseItems || canSubscribe;
    }

    private void logAndDisable(@NonNull final String message) {
        log(message);
        available = false;
    }

    private int getResponseCode(@NonNull final Intent intent) {
        final Bundle extras = intent.getExtras();
        return getResponseCode(extras);
    }

    private int getResponseCode(@Nullable final Bundle bundle) {
        if (bundle == null) {
            log("Null response code from bundle, assuming OK (known issue)");
            return BILLING_RESPONSE_RESULT_OK;
        }

        final Object o = bundle.get(RESPONSE_CODE);
        if (o == null) {
            log("Null response code from bundle, assuming OK (known issue)");
            return BILLING_RESPONSE_RESULT_OK;
        } else if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof Long) {
            return ((Long) o).intValue();
        } else {
            final String message
                    = "Unexpected type for bundle response code. " + o.getClass().getName();
            log(message);
            throw new RuntimeException(message);
        }
    }

    private int purchaseCode(final int response) {
        switch (response) {
            case BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
            case BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
                return Vendor.PURCHASE_UNAVAILABLE;
            case BILLING_RESPONSE_RESULT_USER_CANCELED:
                return Vendor.PURCHASE_CANCELED;
            case BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED:
                return Vendor.PURCHASE_ALREADY_OWNED;
            case BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED:
                return Vendor.PURCHASE_NOT_OWNED;
            case BILLING_RESPONSE_RESULT_DEVELOPER_ERROR:
            case BILLING_RESPONSE_RESULT_ERROR:
            default:
                return Vendor.PURCHASE_FAILURE;
        }
    }

    private int consumeCode(final int response) {
        switch (response) {
            case BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
            case BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
                return Vendor.CONSUME_UNAVAILABLE;
            case BILLING_RESPONSE_RESULT_USER_CANCELED:
                return Vendor.CONSUME_CANCELED;
            case BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED:
                return Vendor.CONSUME_NOT_OWNED;
            case BILLING_RESPONSE_RESULT_DEVELOPER_ERROR:
            case BILLING_RESPONSE_RESULT_ERROR:
            default:
                return Vendor.CONSUME_FAILURE;
        }
    }

    private void log(@NonNull final String message) {
        if (logger == null) return;
        logger.log(message);
    }

    private class ApiException extends Exception {
        private final int code;
        public ApiException(final int code) {
            super("Received Billing API response " + code);
            this.code = code;
        }

        public int code() {
            return code;
        }
    }
}
