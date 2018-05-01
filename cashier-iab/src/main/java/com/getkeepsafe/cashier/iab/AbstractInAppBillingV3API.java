/*
 *  Copyright 2017 Keepsafe Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.getkeepsafe.cashier.iab;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;

import com.getkeepsafe.cashier.logging.Logger;

public abstract class AbstractInAppBillingV3API {
  protected static final int API_VERSION = 3;

  protected String packageName;
  protected InAppBillingV3Vendor vendor;
  protected Logger logger;

  public interface LifecycleListener {
    void initialized(boolean success);

    void disconnected();
  }

  public boolean initialize(Context context, InAppBillingV3Vendor vendor,
                            LifecycleListener listener, Logger logger) {
    if (context == null || vendor == null) {
      throw new IllegalArgumentException("Null context or vendor");
    }
    this.packageName = context.getPackageName();
    this.vendor = vendor;
    this.logger = logger;
    return true;
  }

  public abstract boolean available();

  public abstract void dispose(Context context);

  public abstract int isBillingSupported(String itemType) throws RemoteException;

  public abstract Bundle getSkuDetails(String itemType, Bundle skus) throws RemoteException;

  public abstract Bundle getBuyIntent(String sku, String itemType, String developerPayload)
      throws RemoteException;

  public abstract Bundle getPurchases(String itemType, String paginationToken)
      throws RemoteException;

  public abstract int consumePurchase(String purchaseToken) throws RemoteException;

  protected void throwIfUnavailable() {
    if (packageName == null) {
      throw new IllegalStateException("You did not specify the package name");
    }
  }
}
