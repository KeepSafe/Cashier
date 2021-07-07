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

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.getkeepsafe.cashier.CashierPurchase;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;

import org.json.JSONException;
import org.json.JSONObject;

import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.AUTO_RENEWING;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.DEVELOPER_PAYLOAD;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.ORDER_ID;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.PACKAGE_NAME;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.PRODUCT_ID;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.PURCHASE_STATE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.PURCHASE_STATE_CANCELED;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.PURCHASE_STATE_PURCHASED;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.PURCHASE_STATE_REFUNDED;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.PURCHASE_TIME;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants.PURCHASE_TOKEN;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_PURCHASE_DATA;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_SIGNATURE;

public class InAppBillingPurchase implements Parcelable, Purchase {
  public static final String GP_ORDER_ID_TEST = "TEST-ORDER-ID";

  public static final String GP_KEY_PACKAGE_NAME = "gp-package-name";
  public static final String GP_KEY_DATA_SIG = "gp-data-signature";
  public static final String GP_KEY_AUTO_RENEW = "gp-auto-renewing";
  public static final String GP_KEY_PURCHASE_TIME = "gp-purchase-time";
  public static final String GP_KEY_PURCHASE_STATE = "gp-purchase-state";
  public static final String GP_KEY_PURCHASE_DATA = "gp-purchase-data";

  private final Purchase purchase;
  private final String packageName;
  private final String dataSignature;
  private final boolean autoRenewing;
  private final long purchaseTime;
  private final int purchaseState;
  private final String receipt;

  private InAppBillingPurchase(Purchase purchase, String packageName, String dataSignature, boolean autoRenewing, long purchaseTime, int purchaseState, String receipt) {
    this.purchase = purchase;
    this.packageName = packageName;
    this.dataSignature = dataSignature;
    this.autoRenewing = autoRenewing;
    this.purchaseTime = purchaseTime;
    this.purchaseState = purchaseState;
    this.receipt = receipt;
  }

  protected InAppBillingPurchase(Parcel in) {
    purchase = in.readParcelable(Purchase.class.getClassLoader());
    packageName = in.readString();
    dataSignature = in.readString();
    autoRenewing = in.readByte() != 0;
    purchaseTime = in.readLong();
    purchaseState = in.readInt();
    receipt = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(purchase, flags);
    dest.writeString(packageName);
    dest.writeString(dataSignature);
    dest.writeByte((byte) (autoRenewing ? 1 : 0));
    dest.writeLong(purchaseTime);
    dest.writeInt(purchaseState);
    dest.writeString(receipt);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<InAppBillingPurchase> CREATOR = new Creator<InAppBillingPurchase>() {
    @Override
    public InAppBillingPurchase createFromParcel(Parcel in) {
      return new InAppBillingPurchase(in);
    }

    @Override
    public InAppBillingPurchase[] newArray(int size) {
      return new InAppBillingPurchase[size];
    }
  };

  public Purchase purchase() {
    return purchase;
  }

  /**
   * The application package from which the purchase originated
   */
  public String packageName() {
    return packageName;
  }

  /**
   * String containing the signature of the purchase data that was signed with the private key
   * of the developer.
   */
  public String dataSignature() {
    return dataSignature;
  }

  /**
   * Indicates whether a subscription renews automatically. {@code false} indicates a canceled
   * subscription.
   */
  public boolean autoRenewing() {
    return autoRenewing;
  }

  /**
   * The time the product was purchased, in milliseconds since the UNIX epoch
   */
  public long purchaseTime() {
    return purchaseTime;
  }

  /**
   * The purchase state of the order.
   * Possible values are:
   * <ul>
   * <li>{@code 0} - Purchased</li>
   * <li>{@code 1} - Canceled</li>
   * <li>{@code 2} - Refunded</li>
   * </ul>
   */
  public int purchaseState() {
    return purchaseState;
  }

  /**
   * The original purchase data receipt from Google Play. This is useful for data signature
   * validation
   */
  public String receipt() {
    return receipt;
  }

  public Product product() {
    return purchase().product();
  }

  public String orderId() {
    return purchase().orderId();
  }

  public String token() {
    return purchase().token();
  }

  public String developerPayload() {
    return purchase().developerPayload();
  }

  public static InAppBillingPurchase create(String json) throws JSONException {
    return create(new JSONObject(json));
  }

  public static InAppBillingPurchase create(JSONObject json) throws JSONException {
    final Purchase purchase = CashierPurchase.create(json);
    final String packageName = json.getString(GP_KEY_PACKAGE_NAME);
    final String dataSignature = json.getString(GP_KEY_DATA_SIG);
    final String purchaseData = json.getString(GP_KEY_PURCHASE_DATA);
    final boolean autoRenew = json.getBoolean(GP_KEY_AUTO_RENEW);
    final long purchaseTime = json.getLong(GP_KEY_PURCHASE_TIME);
    final int purchaseState = json.getInt(GP_KEY_PURCHASE_STATE);

    return create(purchase,
        packageName,
        dataSignature,
        autoRenew,
        purchaseTime,
        purchaseState,
        purchaseData);
  }

  public static InAppBillingPurchase create(Product product, Intent purchaseIntent)
      throws JSONException {
    if (product == null || purchaseIntent == null) {
      throw new IllegalArgumentException("Product or purchase intent is null");
    }

    final String purchaseData = purchaseIntent.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA);
    final String dataSignature = purchaseIntent.getStringExtra(RESPONSE_INAPP_SIGNATURE);

    if (purchaseData == null || dataSignature == null) {
      throw new IllegalStateException("The given purchase intent is malformed");
    }

    return create(product, purchaseData, dataSignature);
  }

  public static InAppBillingPurchase create(Product product, String purchaseData,
                                            String dataSignature) throws JSONException {
    if (product == null || purchaseData == null || dataSignature == null) {
      throw new IllegalArgumentException("Product or purchase data or signature is null");
    }

    final JSONObject data = new JSONObject(purchaseData);
    final String packageName = data.getString(PACKAGE_NAME);
    final String purchaseToken = data.getString(PURCHASE_TOKEN);
    final String developerPayload = data.optString(DEVELOPER_PAYLOAD, "");
    // Test purchases do not have an order id, therefore we default to the test order id when
    // it does not exist.
    final String orderId = data.optString(ORDER_ID, GP_ORDER_ID_TEST);
    final String sku = data.getString(PRODUCT_ID);
    if (!sku.equals(product.sku())) {
      throw new IllegalArgumentException("Received mismatched SKU! "
          + sku + " vs " + product.sku());
    }

    final boolean autoRenewing = data.optBoolean(AUTO_RENEWING, false);
    final long purchaseTime = data.getLong(PURCHASE_TIME);
    final int purchaseState = data.getInt(PURCHASE_STATE);

    final Purchase purchase =
        CashierPurchase.create(product, orderId, purchaseToken, purchaseData, developerPayload, null);

    return create(
        purchase,
        packageName,
        dataSignature,
        autoRenewing,
        purchaseTime,
        purchaseState,
        purchaseData);
  }

  public static InAppBillingPurchase create(Purchase purchase, String packageName,
                                            String dataSignature, boolean autoRenew,
                                            long purchaseTime, int purchaseState,
                                            String purchaseData) {
    return new InAppBillingPurchase(purchase,
        packageName,
        dataSignature,
        autoRenew,
        purchaseTime,
        purchaseState,
        purchaseData);
  }


  public boolean purchased() {
    return purchaseState() == PURCHASE_STATE_PURCHASED;
  }

  public boolean canceled() {
    return purchaseState() == PURCHASE_STATE_CANCELED;
  }

  public boolean refunded() {
    return purchaseState() == PURCHASE_STATE_REFUNDED;
  }

  public boolean isTestPurchase() {
    return GP_ORDER_ID_TEST.equals(orderId());
  }

  @Override
  public JSONObject toJson() throws JSONException {
    final JSONObject object = purchase().toJson();
    object.put(GP_KEY_PACKAGE_NAME, packageName());
    object.put(GP_KEY_DATA_SIG, dataSignature());
    object.put(GP_KEY_AUTO_RENEW, autoRenewing());
    object.put(GP_KEY_PURCHASE_TIME, purchaseTime());
    object.put(GP_KEY_PURCHASE_STATE, purchaseState());
    object.put(GP_KEY_PURCHASE_DATA, receipt());

    return object;
  }
}
