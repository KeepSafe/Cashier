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

package com.getkeepsafe.cashier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.getkeepsafe.cashier.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;

/**
 * The global entry point for all billing related functionality.
 * <p>
 * There should only be one instance of this class for each Activity that hosts a billing flow
 */
public class Cashier {
  private static HashMap<String, VendorFactory> vendorFactories = new HashMap<>(1);

  private final Context context;
  private final Vendor vendor;

  /**
   * Registers a vendor factory for use
   *
   * @param vendorId The vendor's unique package ID
   * @param factory  The {@link VendorFactory} that will create instances of the {@link Vendor}
   */
  public static void putVendorFactory(String vendorId, VendorFactory factory) {
    if (TextUtils.isEmpty(vendorId)) {
      throw new IllegalArgumentException("Invalid vendor id, null or empty");
    }

    vendorFactories.put(vendorId, factory);
  }

  /**
   * @param vendorId The vendor's unique package ID
   * @return the {@link VendorFactory} instance that creates the {@link Vendor} with the given ID
   */
  public static VendorFactory getVendorFactory(String vendorId) {
    if (TextUtils.isEmpty(vendorId)) {
      throw new IllegalArgumentException("Invalid vendor id, null or empty");
    }

    final VendorFactory factory = vendorFactories.get(vendorId);
    if (factory == null) {
      throw new VendorMissingException(vendorId);
    }

    return factory;
  }

  /**
   * Returns a Cashier instance builder depending on the app installer
   */
  public static Builder forInstaller(Context context) {
    final String installer = context
        .getPackageManager()
        .getInstallerPackageName(context.getPackageName());
    return new Builder(context).forVendor(getVendorFactory(installer).create());
  }

  /**
   * Returns a Cashier instance builder for the given vendor
   **/
  public static Builder forVendor(Context context, Vendor vendor) {
    return new Builder(context).forVendor(vendor);
  }

  /**
   * Returns a Cashier instance builder that sold the given {@link Purchase}
   **/
  public static Builder forPurchase(Context context, Purchase purchase) {
    return forProduct(context, purchase.product());
  }

  /**
   * Returns a Cashier instance builder that sells the given {@link Product}
   **/
  public static Builder forProduct(Context context, Product product) {
    return new Builder(context).forVendor(getVendorFactory(product.vendorId()).create());
  }

  /**
   * Returns a product from the given JSON supplied by the vendor it belongs to
   **/
  public static Product productFromVendor(String json) throws JSONException {
    return productFromVendor(new JSONObject(json));
  }

  /**
   * Returns a product from the given JSON supplied by the vendor it belongs to
   **/
  public static Product productFromVendor(JSONObject json) throws JSONException {
    final String vendorId = json.getString(Product.KEY_VENDOR_ID);
    final Vendor vendor = getVendorFactory(vendorId).create();
    return vendor.getProductFrom(json);
  }

  /**
   * Returns a purchase from the given JSON supplied by the vendor it belongs to
   **/
  public static Purchase purchaseFromVendor(String json) throws JSONException {
    return purchaseFromVendor(new JSONObject(json));
  }

  /**
   * Returns a purchase from the given JSON supplied by the vendor it belongs to
   **/
  public static Purchase purchaseFromVendor(JSONObject json) throws JSONException {
    final String vendorId = json.getString(Product.KEY_VENDOR_ID);
    final Vendor vendor = getVendorFactory(vendorId).create();
    return vendor.getPurchaseFrom(json);
  }

  private Cashier(Context context, Vendor vendor) {
    Preconditions.checkNotNull(context, "Context is null");
    Preconditions.checkNotNull(vendor, "Vendor is null");
    this.context = context;
    this.vendor = vendor;
  }

  /**
   * Initiates a purchase flow
   *
   * @param activity The activity that will host the purchase flow
   * @param product  The {@link Product} you wish to buy
   * @param listener The {@link PurchaseListener} to handle the result
   */
  public void purchase(Activity activity, Product product, PurchaseListener listener) {
    purchase(activity, product, null, listener);
  }

  /**
   * Initiates a purchase flow
   *
   * @param activity         The activity that will host the purchase flow
   * @param product          The {@link Product} you wish to buy
   * @param developerPayload Your custom payload to pass along to the {@link Vendor}
   * @param listener         The {@link PurchaseListener} to handle the result
   */
  public void purchase(final Activity activity,
                       final Product product,
                       @Nullable final String developerPayload,
                       final PurchaseListener listener) {
    Preconditions.checkNotNull(product, "Product is null");
    Preconditions.checkNotNull(listener, "PurchaseListener is null");
    vendor.initialize(context, new Vendor.InitializationListener() {
      @Override
      public void initialized() {
        if (!vendor.available() || !vendor.canPurchase(product)) {
          listener.failure(product, new Vendor.Error(VendorConstants.PURCHASE_UNAVAILABLE, -1));
          return;
        }

        final String payload = developerPayload == null ? "" : developerPayload;

        ShadowActivity.action = new Action<Activity>() {
          @Override
          public void run(Activity activity) {
            vendor.purchase(activity, product, payload, listener);
          }
        };
        ShadowActivity.cashier = Cashier.this;
        activity.startActivity(new Intent(activity, ShadowActivity.class));
      }

      @Override
      public void unavailable() {
        listener.failure(product, new Vendor.Error(VendorConstants.PURCHASE_UNAVAILABLE, -1));
      }
    });
  }

  /**
   * Consumes the given purchase
   *
   * @param purchase The {@link Purchase} to consume. Must not be a subscription
   * @param listener The {@link ConsumeListener} to handle the result
   */
  public void consume(final Purchase purchase, final ConsumeListener listener) {
    Preconditions.checkNotNull(purchase, "Purchase is null");
    Preconditions.checkNotNull(listener, "ConsumeListener is null");
    if (purchase.product().isSubscription()) {
      throw new IllegalArgumentException("Cannot consume a subscription type!");
    }
    vendor.initialize(context, new Vendor.InitializationListener() {
      @Override
      public void initialized() {
        if (!vendor.available()) {
          listener.failure(purchase, new Vendor.Error(VendorConstants.CONSUME_UNAVAILABLE, -1));
          return;
        }
        vendor.consume(context, purchase, listener);
      }

      @Override
      public void unavailable() {
        listener.failure(purchase, new Vendor.Error(VendorConstants.CONSUME_UNAVAILABLE, -1));
      }
    });
  }

  /**
   * Returns a list of purchased items from the vendor
   *
   * @param listener {@link InventoryListener} to handle the result
   */
  public void getInventory(final InventoryListener listener) {
    getInventory(null, null, listener);
  }

  /**
   * Returns a list of purchased items and specified products from the vendor
   *
   * @param itemSkus A collection of {@link Product} skus to query the vendor for
   * @param subSkus  A collection of subscription {@link Product} skus to query the vendor for
   * @param listener {@link InventoryListener} to handle the result
   */
  public void getInventory(@Nullable final Collection<String> itemSkus,
                           @Nullable final Collection<String> subSkus,
                           final InventoryListener listener) {
    Preconditions.checkNotNull(listener, "InventoryListener is null");
    vendor.initialize(context, new Vendor.InitializationListener() {
      @Override
      public void initialized() {
        vendor.getInventory(context, itemSkus, subSkus, listener);
      }

      @Override
      public void unavailable() {
        listener.failure(new Vendor.Error(VendorConstants.INVENTORY_QUERY_UNAVAILABLE, -1));
      }
    });
  }

  /**
   * Returns a {@link Product} with up-to-date information for the given SKU or fails with
   * {@link VendorConstants#PRODUCT_DETAILS_NOT_FOUND} if the SKU does not describe any
   * current {@link Product}
   *
   * @param sku            The SKU to lookup details for
   * @param isSubscription Whether the SKU is for a subscription or consumable product
   * @param listener       The {@link ProductDetailsListener} to handle the result
   */
  public void getProductDetails(final String sku, final boolean isSubscription, final ProductDetailsListener listener) {
    Preconditions.checkNotNull(sku, "SKU is null");
    Preconditions.checkNotNull(listener, "ProductDetailsListener is null");
    vendor.initialize(context, new Vendor.InitializationListener() {
      @Override
      public void initialized() {
        vendor.getProductDetails(context, sku, isSubscription, listener);
      }

      @Override
      public void unavailable() {
        listener.failure(new Vendor.Error(VendorConstants.PRODUCT_DETAILS_UNAVAILABLE, -1));
      }
    });
  }

  /**
   * Returns the vendor ID that this Cashier belongs to
   **/
  public String vendorId() {
    return vendor.id();
  }

  /**
   * Runs any cleanup functions the {@link Vendor} may need
   **/
  public void dispose() {
    vendor.dispose(context);
  }

  /**
   * Handles results from separate activities. Use of this function depends on
   * the {@link Vendor}
   */
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    return vendor.onActivityResult(requestCode, resultCode, data);
  }

  public static class Builder {
    private final Context context;
    private Vendor vendor;
    private Logger logger;

    public Builder(Context context) {
      this.context = context;
    }

    public Builder forVendor(Vendor vendor) {
      this.vendor = vendor;
      return this;
    }

    public Builder withLogger(@Nullable Logger logger) {
      this.logger = logger;
      return this;
    }

    public Cashier build() {
      if (logger != null) {
        vendor.setLogger(logger);
      }

      return new Cashier(context, vendor);
    }
  }
}
