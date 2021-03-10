<h1 align="center">
	<img src=".github/ic_launcher.png" alt="Cashier"><br/>
	Cashier
</h1>

[![Build Status](https://travis-ci.org/KeepSafe/Cashier.svg?branch=master)](https://travis-ci.org/KeepSafe/Cashier)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.getkeepsafe.cashier/cashier/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.getkeepsafe.cashier/cashier)
[![Release](https://img.shields.io/github/tag/KeepSafe/Cashier.svg?label=jitpack)](https://jitpack.io/#KeepSafe/Cashier)

A general, easy to use billing provider for Android. Provides support for Google Play's in-app billing.

 **Min SDK:** 9

  [JavaDoc](https://javadoc.jitpack.io/com/github/KeepSafe/Cashier/latest/javadoc/)

## Overview

Managing multiple billing services in your Android app is a painful experience when each billing service has their own way of doing things. It becomes worse when each service imposes their own way of doing things, such as requiring the handling of a billing result in `onActivityResult`.

Cashier takes aim to resolve these issues by providing a single consistent API design with different underlying implementations so that you can write your billing code once and easily swap billing providers.

Cashier also aims to bridge the gap between development testing and production testing. When talking about Google Play's in-app-billing, in order to test the full flow of a subscription purchase you need to create a signed APK, upload it to some release channel, add your email to the list of purchase testers, wait a few (or several) hours and then finally test your code. If your code works (which it likely wont, first try) then you're good, however if it doesn't, you're going to have to do the same thing over again. This is obviously not efficient or very nice to develop for. Cashier solves this issue by simulating Google Play as a fake vendor (under the `iab-debug` module). The fake IAB vendor will respond as if you were talking to Google Play in production. This means you can test your purchase flow while you write the code, rather than creating a signed APK each time. With that, here's a list of features Cashier has:

### Features

  - Google Play's In-App-Billing (IAB) - **Deprecated**
    - Purchasing for products and subscriptions, consuming for consumable products
    - Fake checkout, facilitating faster development
    - Local receipt verification
    - Inventory querying

  - Google Play Billing
    - Purchasing for products and subscriptions, consuming for consumable products
    - Fake checkout, facilitating faster development
    - Local receipt verification
    - Inventory querying
    - For now, developer payload is not supported (will be added in GPB v2)

## Installation

Cashier is distributed using [MavenCentral](https://search.maven.org/artifact/com.getkeepsafe.cashier/cashier).

```groovy
repositories {
  mavenCentral()
}

dependencies {
  compile 'com.getkeepsafe.cashier:cashier:x.x.x' // Core library, required

  // Google Play Billing
  compile 'com.getkeepsafe.cashier:cashier-google-play-billing:x.x.x'
  debugCompile 'com.getkeepsafe.cashier:cashier-google-play-billing-debug:x.x.x' // For fake checkout and testing
}
```

## Usage

General usage is as follows:

```java
// First choose a vendor
final Vendor vendor = new GooglePlayBillingVendor();

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

To test app in debug mode with fake purchase flow:
```java
// Create vendor with fake API implementation
vendor = new GooglePlayBillingVendor(
                new FakeGooglePlayBillingApi(MainActivity.this,
                FakeGooglePlayBillingApi.TEST_PUBLIC_KEY));

// Add products definitions
final Product product = Product.create(
  vendor.id(),              // The vendor that produces this product
  "my.sku",                 // The SKU of the product
  "$0.99",                  // The display price of the product
  "USD",                    // The currency of the display price
  "My Awesome Product",     // The product's title
  "Provides awesomeness!",  // The product's description
  false,                    // Whether the product is a subscription or not (consumable)
  990_000L);                // The product price in micros

FakeGooglePlayBillingApi.addTestProduct(product)
```

```FakeGooglePlayBillingApi``` uses predefined private key to sign purchase receipt.
If you want to verify purchase signature in your code, use corresponding public key defined in
```FakeGooglePlayBillingApi.TEST_PUBLIC_KEY```.

## Migrating from In App Billing to Google Play Billing

All you need to do is change vendor implementation from deprecated `InAppBillingV3Vendor` to `GooglePlayBillingVendor`.
Since both implementations are just different ways to connect to Google Play Store, all your products and purchase
flows remain the same.

1. In your dependencies replace
   ```groovy
   compile 'com.getkeepsafe.cashier:cashier-iab:x.x.x'
   debugCompile 'com.getkeepsafe.cashier:cashier-iab-debug:x.x.x' // For fake checkout and testing
   releaseCompile 'com.getkeepsafe.cashier:cashier-iab-debug-no-op:x.x.x'
   ```
   with
   ```groovy
   compile 'com.getkeepsafe.cashier:cashier-google-play-billing:x.x.x'
   debugCompile 'com.getkeepsafe.cashier:cashier-google-play-billing-debug:x.x.x' // For fake checkout and testing
   ```

2. Replace `InAppBillingV3Vendor` with  `GooglePlayBillingVendor`. To test the app in debug mode use `FakeGooglePlayBillingApi` in place of `FakeAppBillingV3Api`.
Definition of products remains the same, but now you need to add them by calling
```FakeGooglePlayBillingApi.addTestProduct(product)```

3. That's it! Now your app will use the new Google Play Billing API!!

## Sample App

For a buildable / workable sample app, please see the `cashier-sample-google-play-billing` project.

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
