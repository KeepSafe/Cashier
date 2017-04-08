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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Inventory {
  private final List<Purchase> purchases;
  private final List<Product> products;

  public Inventory() {
    purchases = new ArrayList<>();
    products = new ArrayList<>();
  }

  public List<Purchase> purchases() {
    return Collections.unmodifiableList(purchases);
  }

  public void addPurchase(Purchase purchase) {
    purchases.add(purchase);
  }

  public void addPurchases(Collection<? extends Purchase> purchases) {
    this.purchases.addAll(purchases);
  }

  public List<Product> products() {
    return Collections.unmodifiableList(products);
  }

  public void addProduct(Product product) {
    products.add(product);
  }

  public void addProducts(Collection<? extends Product> products) {
    this.products.addAll(products);
  }
}
