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

public class VendorConstants {
  private VendorConstants() {
  }

  // Purchases
  /**
   * Cannot buy the product, either do to no network connectivity or vendor being unavailable
   */
  public static final int PURCHASE_UNAVAILABLE = 0;

  /**
   * User canceled the purchase
   */
  public static final int PURCHASE_CANCELED = 1;

  /**
   * Unknown error
   **/
  public static final int PURCHASE_FAILURE = 2;

  /**
   * User already owns the product
   */
  public static final int PURCHASE_ALREADY_OWNED = 3;

  /**
   * User does not own the product
   */
  public static final int PURCHASE_NOT_OWNED = 4;

  /**
   * CashierPurchase seems to be successful, however the expected result is malformed
   */
  public static final int PURCHASE_SUCCESS_RESULT_MALFORMED = 5;

  // Consuming
  /**
   * Unknown consume error
   */
  public static final int CONSUME_FAILURE = 0;

  /**
   * Cannot consume the purchase, either due to no network connectivity or vendor being
   * unavailable
   */
  public static final int CONSUME_UNAVAILABLE = 1;

  /**
   * User canceled the consume
   */
  public static final int CONSUME_CANCELED = 2;

  /**
   * User doesn't own the purchase to consume
   */
  public static final int CONSUME_NOT_OWNED = 3;

  // Inventory
  /**
   * Unknown inventory querying error
   */
  public static final int INVENTORY_QUERY_FAILURE = 0;

  /**
   * Inventory query returned a malformed response
   */
  public static final int INVENTORY_QUERY_MALFORMED_RESPONSE = 1;

  /**
   * Cannot query the inventory, either due to no network connectivity or the vendor being
   * unavailable
   */
  public static final int INVENTORY_QUERY_UNAVAILABLE = 2;

  // Product details
  /**
   * Details for the product do not exist
   **/
  public static final int PRODUCT_DETAILS_NOT_FOUND = 0;

  /**
   * Details for the product could not be retrieved either due to no network connectivity or
   * the vendor being unavailable
   */
  public static final int PRODUCT_DETAILS_UNAVAILABLE = 1;

  /**
   * Unknown error while querying for the product details
   */
  public static final int PRODUCT_DETAILS_QUERY_FAILURE = 2;
}
