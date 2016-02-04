package com.getkeepsafe.cashier.googleplay;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.getkeepsafe.cashier.utilities.Check;

public abstract class InAppBillingV3API {
    protected static final int API_VERSION = 3;

    protected final String packageName;

    public interface LifecycleListener {
        void initialized(final boolean success);
        void disconnected();
    }

    public InAppBillingV3API(@NonNull final String packageName) {
        this.packageName = Check.notNull(packageName, "Package Name");
    }

    public abstract boolean initialize(@NonNull final Activity activity,
                                       @Nullable final LifecycleListener listener);

    public abstract boolean available();

    public abstract void dispose(@NonNull final Activity activity);

    public abstract int isBillingSupported(@NonNull final String itemType) throws RemoteException;

    public abstract Bundle getSkuDetails(@NonNull final String itemType,
                                         @NonNull final Bundle skus) throws RemoteException;

    public abstract Bundle getBuyIntent(@NonNull final String sku,
                                        @NonNull final String itemType,
                                        @Nullable final String developerPayload)
            throws RemoteException;

    public abstract Bundle getPurchases(@NonNull final String itemType,
                                        @Nullable final String paginationToken)
            throws RemoteException;

    public abstract int consumePurchase(@NonNull final String purchaseToken) throws RemoteException;
}
