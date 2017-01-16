<h1 align="center">
	<img src=".github/ic_launcher.png" alt="Cashier"><br/>
	Cashier 
</h1>

[![Build Status](https://travis-ci.com/KeepSafe/Cashier.svg?token=GKHJnCCyj3zqzwTu3uMu&branch=master)](https://travis-ci.com/KeepSafe/Cashier)

A general billing provider for Android. Supports Google Play's in-app billing v3 and Amazon's in-app purchasing v2 out of the box.

**Min SDK:** 9

## Overview

Managing multiple billing services in your Android app is a painful experience when each billing service has their own way of doing things. Google's In-App billing service, for example, requires IPC calls to the Google Play app on users' phones; while others, such as Amazon's in-app purchasing service requires a broadcast receiver and a listener.

Cashier takes aim to resolve these issues by providing a single consistent API design with differing underlying implementations so that you can write your billing code once, and easily swap billing providers.

## Installation

Cashier is distributed using [jcenter](https://bintray.com/keepsafesoftware/Android/Cashier/view).

```groovy
repositories { 
  jcenter()
}
   
dependencies {
  compile 'com.getkeepsafe.cashier:cashier:0.x.x' // Core library, required
 
  // Google Play
  compile 'com.getkeepsafe.cashier:cashier-iab:0.x.x'
  compile 'com.getkeepsafe.cashier:cashier-iab-debug:0.x.x' // For fake checkout and testing
}
```

## Usage

General usage is as follows:

```java
// First choose a vendor
final Vendor vendor = InAppBillingV3Vendor();

// Get a product to buy
final Product product = Product.create(
  vendor.id(),              // The vendor that produces this product
  "my.sku",                 // The SKU of the product
  "$0.99",                  // The display price of the product
  "USD",                    // The currency of the display price
  "My Awesome Product",     // The product's title
  "Provides awesomeness!",  // The product's description
  false,                    // Whether the product is a subscription or not (consumable)
  990_000L);                // The product price in micros

// Then when you are in your purchasing activity,
// set up your listener
final PurchaseListener listener = new PurchaseListener() {
  @Override
  public void success(Purchase purchase) {
    // Yay, now verify the purchase.receipt() with your backend
  }

  @Override
  public void failure(Product product, Vendor.Error error) {
    // Uh-oh, check error.code to see what went wrong
  }
};

// And kick off the purchase!
final Cashier cashier = Cashier.forVendor(activity, new InAppBillingV3Vendor());
cashier.purchase(activity, product, "my custom dev payload", listener);
```

## Sample App

For a buildable / workable sample app, please see the `cashier-sample` project under `cashier-sample/`.

## License

    Copyright 2017 Keepsafe Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
