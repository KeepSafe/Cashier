package com.getkeepsafe.cashier.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.cashier.Cashier;
import com.getkeepsafe.cashier.ConsumeListener;
import com.getkeepsafe.cashier.Inventory;
import com.getkeepsafe.cashier.InventoryListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.PurchaseListener;
import com.getkeepsafe.cashier.Vendor;
import com.getkeepsafe.cashier.VendorFactory;
import com.getkeepsafe.cashier.googleplay.FakeInAppBillingV3Api;
import com.getkeepsafe.cashier.googleplay.GooglePlayConstants;
import com.getkeepsafe.cashier.googleplay.InAppBillingV3Vendor;
import com.getkeepsafe.cashier.logging.LogCatLogger;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    private Switch useFake;
    private TextView ownedSku;
    private Cashier cashier;
    private ProgressDialog progressDialog;
    private Product testProduct;
    private Purchase purchasedProduct;

    private static final String DEV_PAYLOAD = "hello-cashier!";

    private PurchaseListener purchaseListener = new PurchaseListener() {
        @Override
        public void success(@NonNull final Purchase purchase) {
            Toast.makeText(MainActivity.this, "Purchase success", Toast.LENGTH_SHORT).show();
            setOwnedSku(purchase);
            purchasedProduct = purchase;

            // Double-check payload
            if (!DEV_PAYLOAD.equals(purchase.developerPayload)) {
                throw new RuntimeException("Library has a bug! Contact developers immediately!");
            }

            // This is unnecessary, just to show off how to get a cashier instance off a purchase
            cashier.dispose();
            initCashier();
        }

        @Override
        public void failure(@NonNull final Product product, @NonNull final Vendor.Error error) {
            final String message;
            switch (error.code) {
                case Vendor.PURCHASE_CANCELED:
                    message = "Purchase canceled";
                    break;
                case Vendor.PURCHASE_FAILURE:
                    message = "Purchase failed " + error.vendorCode;
                    break;
                case Vendor.PURCHASE_ALREADY_OWNED:
                    message = "You already own " + product.sku + "!";
                    break;
                case Vendor.PURCHASE_SUCCESS_RESULT_MALFORMED:
                    message = "Malformed response! :(";
                    break;
                case Vendor.PURCHASE_UNAVAILABLE:
                default:
                    message = "Purchase unavailable";
                    break;
            }

            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    };

    private ConsumeListener consumeListener = new ConsumeListener() {
        @Override
        public void success(@NonNull final Purchase purchase) {
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
        public void failure(@NonNull final Purchase purchase, @NonNull final Vendor.Error error) {
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
        public void success(@NonNull final Inventory inventory) {
            if (!inventory.purchases().isEmpty()) {
                purchasedProduct = inventory.purchases().get(0);
                setOwnedSku(purchasedProduct);
            } else {
                Toast.makeText(MainActivity.this, "You have no purchased items", Toast.LENGTH_SHORT).show();
                setOwnedSku();
            }
        }

        @Override
        public void failure(@NonNull final Vendor.Error error) {
            final String message;
            switch (error.code) {
                case Vendor.INVENTORY_QUERY_FAILURE:
                default:
                    message = "Couldn't query the inventory for your vendor!";
                    break;
                case Vendor.INVENTORY_QUERY_MALFORMED_RESPONSE:
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

        testProduct = new Product(
                GooglePlayConstants.VENDOR_PACKAGE,
                "android.test.purchased",
                "$0.99",
                "USD",
                "Test product",
                "This is a test product",
                false,
                990_000L);

        // For test mode
        FakeInAppBillingV3Api.testProducts.add(testProduct);

        try {
            cashier = Cashier.forProduct(this, testProduct)
                    .withLogger(new LogCatLogger())
                    .build();
        } catch (VendorFactory.VendorMissingException e) {
            // Wont happen in sample
        }

        setOwnedSku();
        purchaseItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cashier.purchase(testProduct, DEV_PAYLOAD, purchaseListener);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!cashier.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initCashier() {
        if (useFake.isChecked()) {
            cashier = new Cashier.Builder(MainActivity.this)
                    .forVendor(
                            new InAppBillingV3Vendor(
                                    new FakeInAppBillingV3Api(MainActivity.this)))
                    .withLogger(new LogCatLogger())
                    .build();
        } else if (purchasedProduct != null) {
            try {
                cashier = Cashier
                        .forProduct(MainActivity.this, purchasedProduct)
                        .withLogger(new LogCatLogger())
                        .build();
            } catch (VendorFactory.VendorMissingException e) {
                // Won't happen in sample
            }
        } else {
            try {
                cashier = Cashier.forProduct(MainActivity.this, testProduct)
                        .withLogger(new LogCatLogger())
                        .build();
            } catch (VendorFactory.VendorMissingException e) {
                // Shouldn't happen in sample
            }
        }
    }

    private void setOwnedSku() {
        setOwnedSku(null);
    }

    private void setOwnedSku(@Nullable final Purchase purchase) {
        if (purchase == null) {
            ownedSku.setText("No owned sku");
        } else {
            try {
                ownedSku.setText(
                        "Currently owned SKU: " + purchase.sku
                                + "\nOrder Id: " + purchase.orderId
                                + "\nJSON: " + purchase.toJson());
            } catch (JSONException e) {
                // Shouldn't happen in the sample
                throw new RuntimeException(e);
            }
        }
    }
}
