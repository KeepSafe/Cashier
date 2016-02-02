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
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.getkeepsafe.cashier.ConsumeListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.PurchaseListener;
import com.getkeepsafe.cashier.Vendor;
import com.getkeepsafe.cashier.logging.Logger;
import com.getkeepsafe.cashier.utilities.Check;

import org.json.JSONException;

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
        if (!canPurchase(product)) {
            throw new IllegalArgumentException("Cannot purchase given product!" + product.toString());
        }

        if (inAppBillingService == null || !initialized) {
            throw new IllegalStateException("Trying to purchase without initializing first!");
        }

        log("Constructing buy intent...");
        final String type = product.isSubscription ? PRODUCT_TYPE_SUBSCRIPTION : PRODUCT_TYPE_ITEM;
        try {
            final Bundle buyBundle = inAppBillingService
                    .getBuyIntent(API_VERSION, packageName, product.sku, type, developerPayload);

            final int response = getResponseCode(buyBundle);
            if (response != BILLING_RESPONSE_RESULT_OK) {
                log("Couldn't purchase product! code:" + response);
                listener.failure(product, convertCode(response));
                return;
            }

            final PendingIntent pendingIntent = buyBundle.getParcelable(RESPONSE_BUY_INTENT);
            if (pendingIntent == null) {
                log("Received no pending intent!");
                listener.failure(product, convertCode(response));
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
            listener.failure(product, convertCode(BILLING_RESPONSE_RESULT_ERROR));
        }
    }

    @Override
    public void consume(@NonNull final Activity activity,
                        @NonNull final Purchase purchase,
                        @NonNull final ConsumeListener listener) {
        Check.notNull(activity, "Activity");
        Check.notNull(purchase, "Purchase");
        Check.notNull(listener, "Consume Listener");
        if (purchase.isSubscription) {
            throw new IllegalArgumentException("Cannot consume a subscription!");
        }

        if (inAppBillingService == null || !initialized) {
            throw new IllegalStateException("Trying to purchase without initializing first!");
        }

        try {
            log("Consuming " + purchase.sku + " " + purchase.token);
            final int response
                    = inAppBillingService.consumePurchase(API_VERSION, packageName, purchase.token);
            if (response == BILLING_RESPONSE_RESULT_OK) {
                log("Successfully consumed purchase!");
                listener.success(purchase);
            } else {
                log("Couldn't consume purchase! " + response);
                listener.failure(purchase, convertCode(response));
            }
        } catch (RemoteException e) {
            log("Couldn't consume purchase! " + Log.getStackTraceString(e));
            listener.failure(purchase, convertCode(BILLING_RESPONSE_RESULT_ERROR));
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
            purchaseListener.failure(pendingProduct, convertCode(BILLING_RESPONSE_RESULT_ERROR));
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
            purchaseListener.failure(pendingProduct, convertCode(responseCode));
        } else {
            log("Purchase canceled! " + responseCode);
            purchaseListener.failure(pendingProduct, convertCode(responseCode));
        }

        return true;
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

    private int convertCode(final int response) {
        switch (response) {
            case BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
            case BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
                return Vendor.PURCHASE_UNAVAILABLE;
            case BILLING_RESPONSE_RESULT_USER_CANCELED:
                return Vendor.PURCHASE_CANCEL;
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

    private void log(@NonNull final String message) {
        if (logger == null) return;
        logger.log(message);
    }
}
