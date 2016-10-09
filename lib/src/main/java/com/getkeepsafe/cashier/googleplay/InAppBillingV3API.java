package com.getkeepsafe.cashier.googleplay;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;



import com.getkeepsafe.cashier.utilities.Check;

public abstract class InAppBillingV3API {
    protected static final int API_VERSION = 3;

    protected String packageName;
    protected InAppBillingV3Vendor vendor;

    public interface LifecycleListener {
        void initialized(final boolean success);
        void disconnected();
    }

    public boolean initialize(final Context context,
                              final InAppBillingV3Vendor vendor,
                              final LifecycleListener listener) {
        this.packageName = context.getPackageName();
        this.vendor = Check.notNull(vendor, "IAB Vendor");
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
