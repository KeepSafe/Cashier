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

package com.getkeepsafe.cashier.sample.googleplaybilling;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.getkeepsafe.cashier.Cashier;
import com.getkeepsafe.cashier.ConsumeListener;
import com.getkeepsafe.cashier.Inventory;
import com.getkeepsafe.cashier.InventoryListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.ProductDetailsListener;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.PurchaseListener;
import com.getkeepsafe.cashier.Vendor;
import com.getkeepsafe.cashier.VendorMissingException;
import com.getkeepsafe.cashier.billing.GooglePlayBillingConstants;
import com.getkeepsafe.cashier.billing.GooglePlayBillingVendor;
import com.getkeepsafe.cashier.billing.debug.FakeGooglePlayBillingApi;
import com.getkeepsafe.cashier.logging.LogcatLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.getkeepsafe.cashier.VendorConstants.INVENTORY_QUERY_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.INVENTORY_QUERY_MALFORMED_RESPONSE;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_ALREADY_OWNED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_CANCELED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_SUCCESS_RESULT_MALFORMED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_UNAVAILABLE;

public class MainActivity extends AppCompatActivity {
    private Switch useFake;
    private Cashier cashier;
    private ProgressDialog progressDialog;
    private Product testProduct;
    private Product testProduct2;

    private RecyclerView recyclerView;
    private ItemsAdapter itemsAdapter;
    private ProgressBar progressBar;

    private PurchaseListener purchaseListener = new PurchaseListener() {
        @Override
        public void success(Purchase purchase) {
            Toast.makeText(MainActivity.this, "Purchase success", Toast.LENGTH_SHORT).show();
            refreshItems();
        }

        @Override
        public void failure(Product product, Vendor.Error error) {
            final String message;
            switch (error.code) {
                case PURCHASE_CANCELED:
                    message = "Purchase canceled";
                    break;
                case PURCHASE_FAILURE:
                    message = "Purchase failed " + error.vendorCode;
                    break;
                case PURCHASE_ALREADY_OWNED:
                    message = "You already own " + product.sku() + "!";
                    break;
                case PURCHASE_SUCCESS_RESULT_MALFORMED:
                    message = "Malformed response! :(";
                    break;
                case PURCHASE_UNAVAILABLE:
                default:
                    message = "Purchase unavailable";
                    break;
            }

            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    };

    private ConsumeListener consumeListener = new ConsumeListener() {
        @Override
        public void success(Purchase purchase) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Purchase consumed!", Toast.LENGTH_SHORT).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    refreshItems();
                }
            });
        }

        @Override
        public void failure(Purchase purchase, final Vendor.Error error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Did not consume purchase! " + error.code, Toast.LENGTH_SHORT).show();
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            });
        }
    };

    private InventoryListener inventoryListener = new InventoryListener() {
        @Override
        public void success(final Inventory inventory) {
            Map<String, Purchase> purchases = new HashMap<>();
            for (Purchase purchase : inventory.purchases()) {
                purchases.put(purchase.product().sku(), purchase);
            }

            List<Item> items = new ArrayList<>();
            for (Product product : inventory.products()) {
                Item item = new Item();
                item.product = product;
                item.purchase = purchases.get(product.sku());
                item.title = product.name();
                item.price = product.price();
                item.isSubscription = product.isSubscription();
                item.isPurchased = item.purchase != null;
                items.add(item);
            }
            itemsAdapter.setItems(items);

            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void failure(final Vendor.Error error) {
            final String message;
            switch (error.code) {
                case INVENTORY_QUERY_FAILURE:
                default:
                    message = "Couldn't query the inventory for your vendor!";
                    break;
                case INVENTORY_QUERY_MALFORMED_RESPONSE:
                    message = "Query was successful but the vendor returned a malformed response";
                    break;
            }

            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    };

    private ProductDetailsListener productDetailsListener = new ProductDetailsListener() {
        @Override
        public void success(Product product) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Product details")
                    .setMessage("Product details:\n"+
                                "Name: "+product.name()+"\n"+
                                "SKU: "+product.sku()+"\n"+
                                "Price: "+product.price()+"\n"+
                                "Is sub: "+product.isSubscription()+"\n"+
                                "Currency: "+product.currency())
                    .setPositiveButton("OK", null)
                    .create()
                    .show();
        }

        @Override
        public void failure(Vendor.Error error) {
            Toast.makeText(MainActivity.this, "Product details failure!", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);
        useFake = findViewById(R.id.use_fake);
        final Button queryPurchases = findViewById(R.id.query_purchases);

        itemsAdapter = new ItemsAdapter(this);

        recyclerView = findViewById(R.id.items_recycler);
        recyclerView.setAdapter(itemsAdapter);

        progressBar = findViewById(R.id.items_progress);
        progressBar.setVisibility(View.GONE);

        itemsAdapter.setItemListener(new ItemsAdapter.ItemListener() {
            @Override
            public void onItemBuy(Item item) {
                cashier.purchase(MainActivity.this, item.product, purchaseListener);
            }

            @Override
            public void onItemUse(final Item item) {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setTitle("Consuming item, please wait...");
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        cashier.consume(item.purchase, consumeListener);
                    }
                }).start();
            }

            @Override
            public void onItemGetDetails(Item item) {
                cashier.getProductDetails(item.product.sku(), item.isSubscription, productDetailsListener);
            }
        });

        testProduct = Product.create(
                GooglePlayBillingConstants.VENDOR_PACKAGE,
                "android.test.purchased",
                "$0.99",
                "USD",
                "Test product",
                "This is a test product",
                false,
                990_000L);

        testProduct2 = Product.create(
                GooglePlayBillingConstants.VENDOR_PACKAGE,
                "com.abc.def.123",
                "$123.99",
                "USD",
                "Test product 2",
                "This is another test product",
                false,
                123990_000L);

        // For testing certain products
        FakeGooglePlayBillingApi.addTestProduct(testProduct);
        FakeGooglePlayBillingApi.addTestProduct(testProduct2);

        initCashier();

        queryPurchases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshItems();
            }
        });

        useFake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initCashier();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cashier.dispose();
    }

    private void initCashier() {
        if (cashier != null) {
            cashier.dispose();
        }

        new Thread() {
            public void run() {
                Vendor vendor;
                if (useFake.isChecked()) {
                    vendor = new GooglePlayBillingVendor(
                            new FakeGooglePlayBillingApi(MainActivity.this,
                                    FakeGooglePlayBillingApi.TEST_PUBLIC_KEY));
                } else {
                    vendor = new GooglePlayBillingVendor();
                }
                try {
                    cashier = Cashier.forVendor(MainActivity.this, vendor)
                            .withLogger(new LogcatLogger())
                            .build();
                } catch (VendorMissingException e) {
                    // Wont happen in sample
                }
            }
        }.start();
    }

    private void refreshItems() {
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        List<String> itemSkus = new ArrayList<>();
        itemSkus.add(testProduct.sku());
        itemSkus.add(testProduct2.sku());
        cashier.getInventory(itemSkus, null, inventoryListener);
    }
}
