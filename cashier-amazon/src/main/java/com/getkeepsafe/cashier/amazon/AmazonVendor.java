package com.getkeepsafe.cashier.amazon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.RequestId;
import com.amazon.device.iap.model.UserData;
import com.amazon.device.iap.model.UserDataResponse;
import com.getkeepsafe.cashier.ConsumeListener;
import com.getkeepsafe.cashier.InventoryListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.ProductDetailsListener;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.PurchaseListener;
import com.getkeepsafe.cashier.Vendor;
import com.getkeepsafe.cashier.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AmazonVendor implements Vendor {
  final PurchasingListener purchasingListener = new PurchasingListener() {
    @Override
    public void onUserDataResponse(UserDataResponse userDataResponse) {
      if (userDataResponse.getRequestStatus() != UserDataResponse.RequestStatus.SUCCESSFUL) {
        initializationListener.unavailable();
        userId = null;
        currentMarketplace = null;
        return;
      }
      if (userDataResponse.getRequestId().equals(getUserDataRequestId)) {
        final UserData userData = userDataResponse.getUserData();
        userId = userData.getUserId();
        currentMarketplace = userData.getMarketplace();
        initializationListener.initialized();
      }
    }

    @Override
    public void onProductDataResponse(ProductDataResponse productDataResponse) {

    }

    @Override
    public void onPurchaseResponse(PurchaseResponse purchaseResponse) {

    }

    @Override
    public void onPurchaseUpdatesResponse(PurchaseUpdatesResponse purchaseUpdatesResponse) {

    }
  };

  private InitializationListener initializationListener;
  private InventoryListener inventoryListener;
  private RequestId getUserDataRequestId;

  private String userId;
  private String currentMarketplace;
  private boolean registered;

  @Override
  public String id() {
    return "com.amazon.venezia";
  }

  @Override
  public void initialize(Context context, InitializationListener listener) {
    if (context == null || listener == null) {
      throw new IllegalArgumentException("Given null context or listener");
    }

    initializationListener = listener;

    if (available()) {
      listener.initialized();
      return;
    }

    PurchasingService.registerListener(context, purchasingListener);
    getUserDataRequestId = PurchasingService.getUserData();
    registered = true;
  }

  @Override
  public void dispose(Context context) {

  }

  @Override
  public void purchase(Activity activity, Product product, String developerPayload, PurchaseListener listener) {

  }

  @Override
  public void consume(Context context, Purchase purchase, ConsumeListener listener) {

  }

  @Override
  public void getInventory(Context context, List<String> itemSkus, List<String> subSkus, InventoryListener listener) {
    if (context == null || listener == null) {
      throw new IllegalArgumentException("Context or listener is null");
    }

    throwIfUninitialized();

    inventoryListener = listener;
    PurchasingService.getPurchaseUpdates(true);
  }

  @Override
  public void getProductDetails(Context context, String sku, boolean isSubscription, ProductDetailsListener listener) {

  }

  @Override
  public void setLogger(Logger logger) {

  }

  @Override
  public boolean available() {
    return registered && !TextUtils.isEmpty(userId) && !TextUtils.isEmpty(currentMarketplace);
  }

  @Override
  public boolean canPurchase(Product product) {
    return false;
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    return false;
  }

  @Override
  public Product getProductFrom(JSONObject json) throws JSONException {
    return null;
  }

  @Override
  public Purchase getPurchaseFrom(JSONObject json) throws JSONException {
    return null;
  }

  private void throwIfUninitialized() {
    if (!available()) {
      throw new IllegalStateException("Trying to purchase without initializing first!");
    }
  }
}
