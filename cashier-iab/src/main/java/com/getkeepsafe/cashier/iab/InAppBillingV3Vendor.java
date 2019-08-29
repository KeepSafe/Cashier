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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.getkeepsafe.cashier.ConsumeListener;
import com.getkeepsafe.cashier.Inventory;
import com.getkeepsafe.cashier.InventoryListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.ProductDetailsListener;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.PurchaseListener;
import com.getkeepsafe.cashier.Vendor;
import com.getkeepsafe.cashier.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.getkeepsafe.cashier.VendorConstants.CONSUME_CANCELED;
import static com.getkeepsafe.cashier.VendorConstants.CONSUME_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.CONSUME_NOT_OWNED;
import static com.getkeepsafe.cashier.VendorConstants.CONSUME_UNAVAILABLE;
import static com.getkeepsafe.cashier.VendorConstants.INVENTORY_QUERY_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.INVENTORY_QUERY_MALFORMED_RESPONSE;
import static com.getkeepsafe.cashier.VendorConstants.PRODUCT_DETAILS_NOT_FOUND;
import static com.getkeepsafe.cashier.VendorConstants.PRODUCT_DETAILS_QUERY_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_ALREADY_OWNED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_CANCELED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_NOT_OWNED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_SUCCESS_RESULT_MALFORMED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_UNAVAILABLE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_DEVELOPER_ERROR;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_ERROR;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_OK;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_USER_CANCELED;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.INAPP_CONTINUATION_TOKEN;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PRODUCT_TYPE_ITEM;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PRODUCT_TYPE_SUBSCRIPTION;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.REQUEST_SKU_DETAILS_ITEM_LIST;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_BUY_INTENT;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_CODE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_GET_SKU_DETAILS_LIST;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_ITEM_LIST;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_PURCHASE_DATA_LIST;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_SIGNATURE_LIST;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.VENDOR_PACKAGE;

/**
 * @deprecated Google In App Billing is no longer supported.
 * Please use GooglePlayBillingVendor that uses new Google Play Billing library.
 */
@Deprecated
public class InAppBillingV3Vendor implements Vendor {
  private final AbstractInAppBillingV3API api;
  private final String publicKey64;

  private Logger logger;
  private String developerPayload;
  private Product pendingProduct;
  private PurchaseListener purchaseListener;
  private InitializationListener initializationListener;

  private int requestCode;
  private boolean available;
  private boolean canSubscribe;
  private boolean canPurchaseItems;

  private final AbstractInAppBillingV3API.LifecycleListener lifecycleListener
      = new AbstractInAppBillingV3API.LifecycleListener() {
    @Override
    public void initialized(boolean success) {
      log("initialized: success=" + success);
      if (!success) {
        logAndDisable("Couldn't create InAppBillingService instance");
        return;
      }

      try {
        canPurchaseItems
            = api.isBillingSupported(PRODUCT_TYPE_ITEM) == BILLING_RESPONSE_RESULT_OK;

        canSubscribe
            = api.isBillingSupported(PRODUCT_TYPE_SUBSCRIPTION) == BILLING_RESPONSE_RESULT_OK;

        available = canPurchaseItems || canSubscribe;
        log("Connected to service and it is " + (available ? "available" : "not available"));
        initializationListener.initialized();
      } catch (RemoteException e) {
        logAndDisable(Log.getStackTraceString(e));
      }
    }

    @Override
    public void disconnected() {
      logAndDisable("Disconnected from service");
    }
  };

  public InAppBillingV3Vendor() {
    this(new InAppBillingV3API(), null);
  }

  public InAppBillingV3Vendor(String publicKey64) {
    this(new InAppBillingV3API(), publicKey64);
  }

  public InAppBillingV3Vendor(AbstractInAppBillingV3API api) {
    this(api, null);
  }

  public InAppBillingV3Vendor(AbstractInAppBillingV3API api, @Nullable String publicKey64) {
    if (api == null) {
      throw new IllegalArgumentException("Null api");
    }

    this.api = api;
    this.publicKey64 = publicKey64;
    available = false;
  }

  @Override
  public String id() {
    return VENDOR_PACKAGE;
  }

  @Override
  public void initialize(Context context, InitializationListener listener) {
    if (context == null || listener == null) {
      throw new IllegalArgumentException("Context or initialization listener is null");
    }

    initializationListener = listener;

    if (available()) {
      listener.initialized();
      return;
    }

    log("Initializing In-App billing v3...");
    available = api.initialize(context, this, lifecycleListener, logger);

    if (!available) {
      initializationListener.unavailable();
    }
  }

  @Override
  public void dispose(Context context) {
    if (context == null) {
      throw new IllegalArgumentException("Given null context");
    }
    log("Disposing self...");
    api.dispose(context);
    available = false;
  }

  @Override
  public boolean available() {
    return available && api.available() && canPurchaseAnything();
  }

  int getRequestCode() {
    return requestCode;
  }

  @Override
  public boolean canPurchase(Product product) {
    if (!canPurchaseAnything()) {
      return false;
    }

    if (product.isSubscription() && !canSubscribe) {
      return false;
    }

    if (!product.isSubscription() && !canPurchaseItems) {
      return false;
    }

    // TODO: Maybe query inventory and match skus

    return true;
  }

  @Override
  public void purchase(Activity activity, Product product, String developerPayload,
                       PurchaseListener listener) {
    if (activity == null || product == null || listener == null) {
      throw new IllegalArgumentException("Activity, product, or listener is null");
    }

    throwIfUninitialized();

    if (!canPurchase(product)) {
      throw new IllegalArgumentException("Cannot purchase given product!" + product.toString());
    }

    log("Constructing buy intent...");
    final String type = product.isSubscription() ? PRODUCT_TYPE_SUBSCRIPTION : PRODUCT_TYPE_ITEM;
    try {
      if (developerPayload == null) {
        this.developerPayload = UUID.randomUUID().toString();
      } else {
        this.developerPayload = developerPayload;
      }

      final Bundle buyBundle = api.getBuyIntent(product.sku(), type, developerPayload);
      final int response = getResponseCode(buyBundle);
      if (response != BILLING_RESPONSE_RESULT_OK) {
        log("Couldn't purchase product! code:" + response);
        listener.failure(product, purchaseError(response));
        return;
      }

      final PendingIntent pendingIntent = buyBundle.getParcelable(RESPONSE_BUY_INTENT);
      if (pendingIntent == null) {
        log("Received no pending intent!");
        listener.failure(product, purchaseError(response));
        return;
      }

      log("Launching buy intent for " + product.sku());
      this.purchaseListener = listener;
      pendingProduct = product;
      requestCode = new Random().nextInt(1024);
      activity.startIntentSenderForResult(pendingIntent.getIntentSender(),
          requestCode,
          new Intent(), 0, 0, 0);
    } catch (RemoteException | IntentSender.SendIntentException e) {
      log("Failed to launch purchase!\n" + Log.getStackTraceString(e));
      listener.failure(product, purchaseError(BILLING_RESPONSE_RESULT_ERROR));
    }
  }

  @Override
  public void consume(Context context, Purchase purchase, ConsumeListener listener) {
    if (context == null || purchase == null || listener == null) {
      throw new IllegalArgumentException("Context, product, or listener is null");
    }

    throwIfUninitialized();

    final Product product = purchase.product();
    if (product.isSubscription()) {
      throw new IllegalArgumentException("Cannot consume a subscription!");
    }

    try {
      log("Consuming " + product.sku() + " " + purchase.token());
      final int response = api.consumePurchase(purchase.token());
      if (response == BILLING_RESPONSE_RESULT_OK) {
        log("Successfully consumed purchase!");
        listener.success(purchase);
      } else {
        log("Couldn't consume purchase! " + response);
        listener.failure(purchase, consumeError(response));
      }
    } catch (RemoteException e) {
      log("Couldn't consume purchase! " + Log.getStackTraceString(e));
      listener.failure(purchase, consumeError(BILLING_RESPONSE_RESULT_ERROR));
    }
  }

  @Override
  public void getInventory(Context context, Collection<String> inappSkus, Collection<String> subSkus,
                           InventoryListener listener) {
    if (context == null || listener == null) {
      throw new IllegalArgumentException("Context or listener is null");
    }

    throwIfUninitialized();

    // Convert the given collections to a list
    final List<String> inappSkusList = inappSkus == null ? null : new ArrayList<>(inappSkus);
    final List<String> subSkusList = subSkus == null ? null : new ArrayList<>(subSkus);

    final Inventory inventory = new Inventory();
    try {
      log("Querying inventory...");
      inventory.addPurchases(getPurchases(PRODUCT_TYPE_ITEM));
      inventory.addPurchases(getPurchases(PRODUCT_TYPE_SUBSCRIPTION));

      if (inappSkusList != null && !inappSkusList.isEmpty()) {
        inventory.addProducts(getProductsWithType(inappSkusList, PRODUCT_TYPE_ITEM));
      }

      if (subSkusList != null && !subSkusList.isEmpty()) {
        inventory.addProducts(getProductsWithType(subSkusList, PRODUCT_TYPE_SUBSCRIPTION));
      }

      listener.success(inventory);
    } catch (RemoteException | ApiException e) {
      listener.failure(new Vendor.Error(INVENTORY_QUERY_FAILURE, codeFromException(e)));
    } catch (JSONException e) {
      listener.failure(new Vendor.Error(INVENTORY_QUERY_MALFORMED_RESPONSE, -1));
    }
  }

  @Override
  public void getProductDetails(Context context, String sku, boolean isSubscription,
                                ProductDetailsListener listener) {
    if (context == null || sku == null || listener == null) {
      throw new IllegalArgumentException("Context or sku or listener is null");
    }
    throwIfUninitialized();
    final String type = isSubscription ? PRODUCT_TYPE_SUBSCRIPTION : PRODUCT_TYPE_ITEM;
    try {
      final List<Product> productList = getProductsWithType(Collections.singletonList(sku), type);
      if (productList.isEmpty()) {
        listener.failure(new Vendor.Error(PRODUCT_DETAILS_NOT_FOUND, -1));
        return;
      }

      listener.success(productList.get(0));
    } catch (RemoteException | ApiException e) {
      listener.failure(new Vendor.Error(PRODUCT_DETAILS_QUERY_FAILURE, codeFromException(e)));
    }
  }

  private List<InAppBillingPurchase> getPurchases(String type)
      throws RemoteException, ApiException, JSONException {
    throwIfUninitialized();
    if (type.equals(PRODUCT_TYPE_ITEM)) {
      log("Querying item purchases...");
    } else {
      log("Querying subscription purchases...");
    }
    String paginationToken = null;
    final List<InAppBillingPurchase> purchaseList = new ArrayList<>();
    do {
      final Bundle purchases = api.getPurchases(type, paginationToken);

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

      final List<Product> purchasedProducts = getProductsWithType(purchasedSkus, type);

      for (int i = 0; i < purchaseDataList.size(); ++i) {
        final String purchaseData = purchaseDataList.get(i);
        final String sku = purchasedSkus.get(i);
        final String signature = signatureList.get(i);

        Product product = null;
        for (final Product maybeProduct : purchasedProducts) {
          if (sku.equals(maybeProduct.sku())) {
            product = maybeProduct;
            break;
          }
        }

        if (product == null) {
          // TODO: Should raise this as an error to the user
          continue;
        }

        log("Found purchase: " + sku);
        if (!TextUtils.isEmpty(publicKey64)) {
          if (InAppBillingSecurity.verifySignature(publicKey64, purchaseData, signature)) {
            log("Purchase locally verified: " + sku);
          } else {
            log("Purchase not locally verified: " + sku);
            continue;
          }
        }

        purchaseList.add(InAppBillingPurchase.create(product, purchaseData, signature));
      }

      paginationToken = purchases.getString(INAPP_CONTINUATION_TOKEN);
      if (paginationToken != null) {
        log("Pagination token found, continuing on....");
      }
    } while (!TextUtils.isEmpty(paginationToken));

    return Collections.unmodifiableList(purchaseList);
  }

  private List<Product> getProductsWithType(List<String> skus, String type)
      throws RemoteException, ApiException {
    if (skus == null || TextUtils.isEmpty(type)) {
      throw new IllegalArgumentException("Given skus are null or type is empty/null");
    }

    throwIfUninitialized();
    log("Retrieving sku details for " + skus.size() + " " + type + " skus");
    if (!type.equals(PRODUCT_TYPE_ITEM) && !type.equals(PRODUCT_TYPE_SUBSCRIPTION)) {
      throw new IllegalArgumentException("Invalid product type " + type);
    }

    final List<Product> products = new ArrayList<>();
    for (int i = 0; i < skus.size(); i += 20) {
      final ArrayList<String> page = new ArrayList<>(skus.subList(i, Math.min(skus.size(), i + 20)));
      final Bundle skuQuery = new Bundle();
      skuQuery.putStringArrayList(REQUEST_SKU_DETAILS_ITEM_LIST, page);

      final Bundle skuDetails = api.getSkuDetails(type, skuQuery);
      final int response = getResponseCode(skuDetails);
      log("Got response: " + response);
      if (skuDetails == null) {
        continue;
      }

      if (response != BILLING_RESPONSE_RESULT_OK || !skuDetails.containsKey(RESPONSE_GET_SKU_DETAILS_LIST)) {
        throw new ApiException(response);
      }

      final ArrayList<String> detailsList
          = skuDetails.getStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST);
      if (detailsList == null) continue;

      for (final String detail : detailsList) {
        log("Parsing sku details: " + detail);
        try {
          products.add(InAppBillingProduct.create(detail, type.equals(PRODUCT_TYPE_SUBSCRIPTION)));
        } catch (JSONException e) {
          log("Couldn't parse sku: " + detail);
        }
      }
    }

    return Collections.unmodifiableList(products);
  }

  @Override
  public void setLogger(@Nullable Logger logger) {
    this.logger = logger;
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    log("onActivityResult " + resultCode);
    if (this.requestCode != requestCode) {
      return false;
    }

    if (data == null) {
      purchaseListener.failure(pendingProduct, purchaseError(BILLING_RESPONSE_RESULT_ERROR));
      return true;
    }

    final int responseCode = getResponseCode(data);
    if (resultCode == Activity.RESULT_OK && responseCode == BILLING_RESPONSE_RESULT_OK) {
      try {
        final InAppBillingPurchase purchase = InAppBillingPurchase.create(pendingProduct, data);
        if (!purchase.developerPayload().equals(developerPayload)) {
          log("Developer payload mismatch!");
          purchaseListener.failure(pendingProduct,
              new Vendor.Error(PURCHASE_SUCCESS_RESULT_MALFORMED,
                  BILLING_RESPONSE_RESULT_ERROR));
          return true;
        }

        if (!TextUtils.isEmpty(publicKey64)
            && !InAppBillingSecurity.verifySignature(publicKey64, purchase.receipt(), purchase.dataSignature())) {
          log("Local signature check failed!");
          purchaseListener.failure(pendingProduct,
              new Vendor.Error(PURCHASE_SUCCESS_RESULT_MALFORMED,
                  BILLING_RESPONSE_RESULT_ERROR));
          return true;
        }

        log("Successful purchase of " + pendingProduct.sku() + "!");
        purchaseListener.success(purchase);
        developerPayload = null;
      } catch (JSONException e) {
        purchaseListener.failure(pendingProduct,
            new Vendor.Error(PURCHASE_SUCCESS_RESULT_MALFORMED,
                BILLING_RESPONSE_RESULT_ERROR));
      }
    } else if (resultCode == Activity.RESULT_OK) {
      log("CashierPurchase failed! " + responseCode);
      purchaseListener.failure(pendingProduct, purchaseError(responseCode));
    } else {
      log("CashierPurchase canceled! " + responseCode);
      purchaseListener.failure(pendingProduct, purchaseError(responseCode));
    }

    return true;
  }

  @Override
  public Product getProductFrom(JSONObject json) throws JSONException {
    final Product product = Product.create(json);
    if (!product.vendorId().equals(VENDOR_PACKAGE)) {
      throw new IllegalArgumentException("This product does not belong to Google Play");
    }

    return product;
  }

  @Override
  public Purchase getPurchaseFrom(JSONObject json) throws JSONException {
    final InAppBillingPurchase purchase = InAppBillingPurchase.create(json);
    if (!purchase.product().vendorId().equals(VENDOR_PACKAGE)) {
      throw new IllegalArgumentException("This purchase does not belong to Google Play");
    }

    return purchase;
  }

  private void throwIfUninitialized() {
    if (!api.available()) {
      throw new IllegalStateException("Trying to purchase without initializing first!");
    }
  }

  private boolean canPurchaseAnything() {
    return canPurchaseItems || canSubscribe;
  }

  private void logAndDisable(String message) {
    log(message);
    available = false;
  }

  private int getResponseCode(Intent intent) {
    final Bundle extras = intent.getExtras();
    return getResponseCode(extras);
  }

  private int getResponseCode(Bundle bundle) {
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
      final String message = "Unexpected type for bundle response code. " + o.getClass().getName();
      log(message);
      throw new RuntimeException(message);
    }
  }

  private int codeFromException(Exception e) {
    if (e instanceof ApiException) {
      return ((ApiException) e).code;
    }

    return -1;
  }

  private Vendor.Error purchaseError(int response) {
    final int code;
    switch (response) {
      case BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
      case BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
        code = PURCHASE_UNAVAILABLE;
        break;
      case BILLING_RESPONSE_RESULT_USER_CANCELED:
        code = PURCHASE_CANCELED;
        break;
      case BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED:
        code = PURCHASE_ALREADY_OWNED;
        break;
      case BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED:
        code = PURCHASE_NOT_OWNED;
        break;
      case BILLING_RESPONSE_RESULT_DEVELOPER_ERROR:
      case BILLING_RESPONSE_RESULT_ERROR:
      default:
        code = PURCHASE_FAILURE;
        break;
    }

    return new Vendor.Error(code, response);
  }

  private Vendor.Error consumeError(int response) {
    final int code;
    switch (response) {
      case BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE:
      case BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE:
        code = CONSUME_UNAVAILABLE;
        break;
      case BILLING_RESPONSE_RESULT_USER_CANCELED:
        code = CONSUME_CANCELED;
        break;
      case BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED:
        code = CONSUME_NOT_OWNED;
        break;
      case BILLING_RESPONSE_RESULT_DEVELOPER_ERROR:
      case BILLING_RESPONSE_RESULT_ERROR:
      default:
        code = CONSUME_FAILURE;
        break;
    }

    return new Vendor.Error(code, response);
  }

  private void log(String message) {
    if (logger == null) return;
    logger.i("InAppBillingV3Vendor", message);
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
