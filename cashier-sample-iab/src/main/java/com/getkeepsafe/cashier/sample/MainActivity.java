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

package com.getkeepsafe.cashier.sample;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
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
import com.getkeepsafe.cashier.VendorFactory;
import com.getkeepsafe.cashier.VendorMissingException;
import com.getkeepsafe.cashier.iab.InAppBillingConstants;
import com.getkeepsafe.cashier.iab.InAppBillingV3Vendor;
import com.getkeepsafe.cashier.iab.debug.FakeInAppBillingV3Api;
import com.getkeepsafe.cashier.logging.LogcatLogger;

import org.json.JSONException;

import java.util.Arrays;
import java.util.List;

import static com.getkeepsafe.cashier.VendorConstants.INVENTORY_QUERY_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.INVENTORY_QUERY_MALFORMED_RESPONSE;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_ALREADY_OWNED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_CANCELED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_SUCCESS_RESULT_MALFORMED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_UNAVAILABLE;

public class MainActivity extends AppCompatActivity {
    private Switch useFake;
    private TextView ownedSku;
    private Cashier cashier;
    private ProgressDialog progressDialog;
    private Product testProduct;
    private Product testProduct2;
    private Purchase purchasedProduct;

    private static final String DEV_PAYLOAD = "hello-cashier!";

    private PurchaseListener purchaseListener = new PurchaseListener() {
        @Override
        public void success(Purchase purchase) {
            Toast.makeText(MainActivity.this, "Purchase success", Toast.LENGTH_SHORT).show();
            setOwnedSku(purchase);
            purchasedProduct = purchase;

            // Double-check payload
            if (!DEV_PAYLOAD.equals(purchase.developerPayload())) {
                throw new RuntimeException("Library has a bug! Contact developers immediately!");
            }

//            // This is unnecessary, just to show off how to get a cashier instance off a purchase
//            cashier.dispose();
//            initCashier();
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
                    setOwnedSku();
                    purchasedProduct = null;
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
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
            if (!inventory.purchases().isEmpty()) {
                purchasedProduct = inventory.purchases().get(0);
                setOwnedSku(purchasedProduct);
            } else {
                Toast.makeText(MainActivity.this, "You have no purchased items", Toast.LENGTH_SHORT).show();
                setOwnedSku();
            }
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
        }
    };

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);
        ownedSku = (TextView) findViewById(R.id.current_owned_sku);
        useFake = (Switch) findViewById(R.id.use_fake);
        final Button purchaseItem = (Button) findViewById(R.id.buy_item);
        final Button consumeItem = (Button) findViewById(R.id.consume_item);
        final Button queryPurchases = (Button) findViewById(R.id.query_purchases);
        final Button querySku = (Button) findViewById(R.id.query_sku);
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);

        testProduct = Product.create(
                InAppBillingConstants.VENDOR_PACKAGE,
                "android.test.purchased",
                "$0.99",
                "USD",
                "Test product",
                "This is a test product",
                false,
                990_000L);

        testProduct2 = Product.create(
                InAppBillingConstants.VENDOR_PACKAGE,
                "com.abc.def.123",
                "$123.99",
                "USD",
                "Test product 2",
                "This is another test product",
                false,
                123990_000L);

        spinner.setAdapter(new ProductSpinnerAdapter(Arrays.asList(testProduct, testProduct2)));

        // For testing certain products
        FakeInAppBillingV3Api.addTestProduct(testProduct);
        FakeInAppBillingV3Api.addTestProduct(testProduct2);

        // This will typically be in your application's `onCreate` method
        Cashier.putVendorFactory(InAppBillingConstants.VENDOR_PACKAGE, new VendorFactory() {
            @Override
            public Vendor create() {
                return new InAppBillingV3Vendor();
            }
        });

        try {
            cashier = Cashier.forProduct(this, testProduct)
                    .withLogger(new LogcatLogger())
                    .build();
        } catch (VendorMissingException e) {
            // Wont happen in sample
        }

        setOwnedSku();
        purchaseItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cashier.purchase(MainActivity.this, (Product) spinner.getSelectedItem(), DEV_PAYLOAD, purchaseListener);
            }
        });

        consumeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (purchasedProduct == null) {
                    Toast.makeText(MainActivity.this, "You need to buy first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setTitle("Consuming item, please wait...");
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        cashier.consume(purchasedProduct, consumeListener);
                    }
                }).start();
            }
        });

        queryPurchases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cashier.getInventory(inventoryListener);
            }
        });

        querySku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cashier.getProductDetails("android.test.purchased", false, new ProductDetailsListener() {
                    @Override
                    public void success(Product product) {
                        try {
                            ownedSku.setText(product.toJsonString());
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void failure(Vendor.Error error) {
                        Toast.makeText(MainActivity.this, "Received error " + error.code + " " + error.vendorCode, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        useFake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cashier != null) {
                    cashier.dispose();
                }

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

        if (useFake.isChecked()) {
            cashier = new Cashier.Builder(MainActivity.this)
                    .forVendor(
                            new InAppBillingV3Vendor(
                                    new FakeInAppBillingV3Api(MainActivity.this), FakeInAppBillingV3Api.TEST_PUBLIC_KEY))
                    .withLogger(new LogcatLogger())
                    .build();
        } else if (purchasedProduct != null) {
            try {
                cashier = Cashier
                        .forPurchase(MainActivity.this, purchasedProduct)
                        .withLogger(new LogcatLogger())
                        .build();
            } catch (VendorMissingException e) {
                // Won't happen in sample
            }
        } else {
            try {
                cashier = Cashier.forProduct(MainActivity.this, testProduct)
                        .withLogger(new LogcatLogger())
                        .build();
            } catch (VendorMissingException e) {
                // Shouldn't happen in sample
            }
        }
    }

    private void setOwnedSku() {
        setOwnedSku(null);
    }

    private void setOwnedSku(Purchase purchase) {
        if (purchase == null) {
            ownedSku.setText("No owned sku");
        } else {
            try {
                ownedSku.setText(
                        "Currently owned SKU: " + purchase.product().sku()
                                + "\nOrder Id: " + purchase.orderId()
                                + "\nJSON: " + purchase.toJson());
            } catch (JSONException e) {
                // Shouldn't happen in the sample
                throw new RuntimeException(e);
            }
        }
    }

    private static class ProductSpinnerAdapter extends BaseAdapter {
        private final List<Product> products;

        public ProductSpinnerAdapter(List<Product> products) {
            this.products = products;
        }

        @Override
        public int getCount() {
            return products.size();
        }

        @Override
        public Product getItem(int position) {
            return products.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TextView textView;
            if (convertView == null || !(convertView instanceof TextView)) {
                textView = new TextView(parent.getContext());
            } else {
                textView = (TextView) convertView;
            }

            final Product product = getItem(position);
            textView.setText(product.name());
            return textView;
        }
    }
}
