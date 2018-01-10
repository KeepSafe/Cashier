<h1 align="center">
	<img src=".github/ic_launcher.png" alt="Cashier"><br/>
	Cashier 
</h1>

[![Build Status](https://travis-ci.org/KeepSafe/Cashier.svg?branch=master)](https://travis-ci.org/KeepSafe/Cashier)

A general, easy to use billing provider for Android. Provides support for Google Play's in-app billing, and soon(tm) Amazon's in-app-billing.

**Min SDK:** 9

## Overview

Managing multiple billing services in your Android app is a painful experience when each billing service has their own way of doing things. It becomes worse when each service imposes their own way of doing things, such as requiring the handling of a billing result in `onActivityResult`.

Cashier takes aim to resolve these issues by providing a single consistent API design with different underlying implementations so that you can write your billing code once and easily swap billing providers.

Cashier also aims to bridge the gap between development testing and production testing. When talking about Google Play's in-app-billing, in order to test the full flow of a subscription purchase you need to create a signed APK, upload it to some release channel, add your email to the list of purchase testers, wait a few (or several) hours and then finally test your code. If your code works (which it likely wont, first try) then you're good, however if it doesnt, you're going to have to do the same thing over again. This is obviously not effecient or very nice to develop for. Cashier solves this issue by simulating Google Play as a fake vendor (under the `iab-debug` module). The fake IAB vendor will respond as if you were talking to Google Play in production. This means you can test your purchase flow while you write the code, rather than creating a signed APK each time. With that, here's a list of features Cashier has:

### Features

  - Google Play's In-App-Billing (IAB)
    - Purchasing for products and subscriptions, consuming for consumable products
    - Fake checkout, facilitating faster development
    - Local receipt verification
    - Inventory querying

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
  debugCompile 'com.getkeepsafe.cashier:cashier-iab-debug:0.x.x' // For fake checkout and testing
  releaseCompile 'com.getkeepsafe.cashier:cashier-iab-debug-no-op:0.x.x'
}
```

## Usage

General usage is as follows:

```java
// First choose a vendor
final Vendor vendor = new InAppBillingV3Vendor();

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
final Cashier cashier = Cashier.forVendor(activity, vendor);
cashier.purchase(activity, product, "my custom dev payload", listener);
```

## Sample App

For a buildable / workable sample app, please see the `cashier-sample` project under `cashier-sample/`.

## Acknowledgements

A very special thank you to [Jeff Young](https://www.github.com/tenoversix) for the awesome logo!

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
