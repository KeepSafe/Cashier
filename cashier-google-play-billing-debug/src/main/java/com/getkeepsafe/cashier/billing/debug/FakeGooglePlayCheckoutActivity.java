package com.getkeepsafe.cashier.billing.debug;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.billing.GooglePlayBillingSecurity;

import org.json.JSONException;
import org.json.JSONObject;

public class FakeGooglePlayCheckoutActivity extends Activity {

    private static final String ARGUMENT_PRODUCT = "product";
    private static final String ARGUMENT_DEV_PAYLOAD = "developer_payload";
    private static final String ARGUMENT_ACCOUNT_ID = "account_id";

    private Product product;

    private String developerPayload;

    private String accountId;

    public static Intent intent(Context context, Product product, String developerPayload, String accountId, String privateKey64) {
        Intent intent = new Intent(context, FakeGooglePlayCheckoutActivity.class);
        intent.putExtra(ARGUMENT_PRODUCT, product);
        intent.putExtra(ARGUMENT_DEV_PAYLOAD, developerPayload);
        intent.putExtra(ARGUMENT_ACCOUNT_ID, accountId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_checkout);

        final Intent intent = getIntent();
        product = intent.getParcelableExtra(ARGUMENT_PRODUCT);
        developerPayload = intent.getStringExtra(ARGUMENT_DEV_PAYLOAD);
        accountId = intent.getStringExtra(ARGUMENT_ACCOUNT_ID);

        final TextView productName = bind(R.id.product_name);
        final TextView productDescription = bind(R.id.product_description);
        final TextView productPrice = bind(R.id.product_price);
        final TextView productMetadata = bind(R.id.product_metadata);
        final Button buyButton = bind(R.id.buy);

        productName.setText(product.name());
        productDescription.setText(product.description());
        productPrice.setText(product.price());

        productMetadata.setText(String.valueOf(
                metadataField("Vendor", product.vendorId())) +
                metadataField("SKU", product.sku()) +
                metadataField("Subscription", product.isSubscription()) +
                metadataField("Micro-price", product.microsPrice()) +
                metadataField("Currency", product.currency()));

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject purchaseJson = new JSONObject();
                    purchaseJson.put("orderId", String.valueOf(System.currentTimeMillis()));
                    purchaseJson.put("purchaseToken", product.sku() + "_" + System.currentTimeMillis());
                    purchaseJson.put("purchaseState", 0);
                    purchaseJson.put("productId", product.sku());
                    purchaseJson.put("developerPayload", developerPayload);
                    purchaseJson.put("obfuscatedAccountId", accountId);
                    String json = purchaseJson.toString();
                    String signature = GooglePlayBillingSecurity.sign(FakeGooglePlayBillingApi.TEST_PRIVATE_KEY, json);
                    Purchase purchase = new Purchase(json, signature);

                    FakeGooglePlayBillingApi.notifyPurchaseSuccess(product.sku(), purchase);

                } catch (JSONException e) {
                    FakeGooglePlayBillingApi.notifyPurchaseError(product.sku(), BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE);
                }
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FakeGooglePlayBillingApi.notifyPurchaseError(product.sku(), BillingClient.BillingResponseCode.USER_CANCELED);
    }

    private SpannableString metadataField(String name, Object value) {
        final SpannableString string = new SpannableString(name + ": " + value.toString() + "\n");
        string.setSpan(new StyleSpan(Typeface.BOLD), 0, name.length() + 1, 0);
        return string;
    }

    @SuppressWarnings("unchecked")
    private <T> T bind(int id) {
        return (T) findViewById(id);
    }
}
