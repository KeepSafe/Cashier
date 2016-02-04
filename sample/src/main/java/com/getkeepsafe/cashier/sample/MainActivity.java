package com.getkeepsafe.cashier.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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
import com.getkeepsafe.cashier.logging.LogCatLogger;

public class MainActivity extends AppCompatActivity {
    private TextView ownedSku;
    private Cashier cashier;
    private ProgressDialog progressDialog;
    private Purchase purchasedProduct;

    private PurchaseListener purchaseListener = new PurchaseListener() {
        @Override
        public void success(@NonNull final Purchase purchase) {
            Toast.makeText(MainActivity.this, "Purchase success", Toast.LENGTH_SHORT).show();
            setOwnedSku(purchase.sku, purchase.orderId, purchase.token);
            purchasedProduct = purchase;
        }

        @Override
        public void failure(@NonNull final Product product, @NonNull final Vendor.Error error) {
            final String message;
            switch (error.code) {
                case Vendor.PURCHASE_CANCELED:
                    message = "Purchase canceled";
                    break;
                case Vendor.PURCHASE_FAILURE:
                    message = "Purchase failed " + error.code;
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
                setOwnedSku(purchasedProduct.sku, purchasedProduct.orderId, purchasedProduct.token);
            } else {
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
        final Button purchaseItem = (Button) findViewById(R.id.buy_item);
        final Button consumeItem = (Button) findViewById(R.id.consume_item);
        final Button queryPurchases = (Button) findViewById(R.id.query_purchases);

        cashier = Cashier.forGooglePlay(this)
                .withLogger(new LogCatLogger())
                .build();

        final Product testProduct = Product.item(
                "android.test.purchased",
                "$0.99",
                "USD",
                "Test product",
                "This is a test product",
                990_000L);

        setOwnedSku();
        purchaseItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cashier.purchase(testProduct, purchaseListener);
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

    private void setOwnedSku() {
        setOwnedSku("None", "None", "None");
    }

    private void setOwnedSku(@NonNull final String sku,
                             @NonNull final String orderId,
                             @NonNull final String token) {
        ownedSku.setText(
                "Currently owned SKU: " + sku
                + "\nOrder Id: " + orderId
                + "\nToken: " + token);
    }
}
