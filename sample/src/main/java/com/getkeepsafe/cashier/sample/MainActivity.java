package com.getkeepsafe.cashier.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.getkeepsafe.cashier.Cashier;
import com.getkeepsafe.cashier.ConsumeListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.PurchaseListener;
import com.getkeepsafe.cashier.Receipt;
import com.getkeepsafe.cashier.Vendor;
import com.getkeepsafe.cashier.googleplay.InAppBillingV3Vendor;
import com.getkeepsafe.cashier.logging.LogCatLogger;

public class MainActivity extends AppCompatActivity {
    private Button purchaseItem;
    private Button consumeItem;
    private InAppBillingV3Vendor gplay;
    private Cashier gplayCashier;

    private PurchaseListener purchaseListener = new PurchaseListener() {
        @Override
        public void success(@NonNull Product product, @NonNull Receipt receipt) {
            Toast.makeText(MainActivity.this, "Purchase success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void failure(@NonNull Product product, int code) {
            final String message;
            switch (code) {
                case Vendor.PURCHASE_CANCEL:
                    message = "Purchase canceled";
                    break;
                case Vendor.PURCHASE_FAILURE:
                    message = "Purchase failed " + code;
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
        public void success(@NonNull Purchase purchase) {
            Toast.makeText(MainActivity.this, "Purchase consumed!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void failure(@NonNull Purchase purchase, int code) {
            Toast.makeText(MainActivity.this, "Did not consume purchase! " + code, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);
        purchaseItem = (Button) findViewById(R.id.buy_item);
        consumeItem = (Button) findViewById(R.id.consume_item);

        gplay = new InAppBillingV3Vendor(getPackageName(), new LogCatLogger());
        gplayCashier = new Cashier(this, gplay);

        final Product testProduct = Product.item("android.test.purchased");
        final Purchase testPurchase
                = new Purchase(testProduct, "inapp:" + getPackageName() + ":android.test.purchased");

        purchaseItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gplayCashier.purchase(testProduct, purchaseListener);
            }
        });

        consumeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gplayCashier.consume(testPurchase, consumeListener);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gplayCashier.dispose();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!gplay.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
