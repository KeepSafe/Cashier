package com.getkeepsafe.cashier.iab;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;

public abstract class InAppBillingV3API {
    protected static final int API_VERSION = 3;

    protected String packageName;
    protected InAppBillingV3Vendor vendor;

    public interface LifecycleListener {
        void initialized(final boolean success);
        void disconnected();
    }

    public boolean initialize(Context context, InAppBillingV3Vendor vendor,
                              LifecycleListener listener) {
        if (context == null || vendor == null) {
            throw new IllegalArgumentException("Null context or vendor");
        }
        this.packageName = context.getPackageName();
        this.vendor = vendor;
        return true;
    }

    public abstract boolean available();

    public abstract void dispose(final Context context);

    public abstract int isBillingSupported(final String itemType) throws RemoteException;

    public abstract Bundle getSkuDetails(final String itemType,
                                         final Bundle skus) throws RemoteException;

    public abstract Bundle getBuyIntent(final String sku,
                                        final String itemType,
                                        final String developerPayload)
            throws RemoteException;

    public abstract Bundle getPurchases(final String itemType,
                                        final String paginationToken)
            throws RemoteException;

    public abstract int consumePurchase(final String purchaseToken) throws RemoteException;

    protected void throwIfUnavailable() {
        if (packageName == null) {
            throw new IllegalStateException("You did not specify the package name");
        }
    }
}
