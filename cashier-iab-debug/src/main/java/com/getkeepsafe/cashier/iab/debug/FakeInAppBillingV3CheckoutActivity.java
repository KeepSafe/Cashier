package com.getkeepsafe.cashier.iab.debug;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.getkeepsafe.cashier.Product;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_OK;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_USER_CANCELED;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.PurchaseConstants;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_CODE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_PURCHASE_DATA;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_SIGNATURE;

public class FakeInAppBillingV3CheckoutActivity extends Activity {
    private static final String ARGUMENT_PRODUCT = "product";
    private static final String ARGUMENT_PACKAGE = "package";
    private static final String ARGUMENT_DEV_PAYLOAD = "dev-payload";
    private static final String ARGUMENT_PRIVATE_KEY = "private-key";

    private Product product;
    private String packageName;
    private String developerPayload;
    private String privateKey64;

    public static PendingIntent pendingIntent(Context context, Product product,
                                              String developerPayload, String privateKey64) {
        final Intent intent = new Intent(context, FakeInAppBillingV3CheckoutActivity.class);
        intent.putExtra(ARGUMENT_PRODUCT, product);
        intent.putExtra(ARGUMENT_PACKAGE, context.getPackageName());
        if (developerPayload != null) {
            intent.putExtra(ARGUMENT_DEV_PAYLOAD, developerPayload);
        }

        if (privateKey64 != null) {
            intent.putExtra(ARGUMENT_PRIVATE_KEY, privateKey64);
        }

        return PendingIntent.getActivity(context, 1337, intent, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        if (savedInstance != null) {
            product = savedInstance.getParcelable(ARGUMENT_PRODUCT);
            packageName = savedInstance.getString(ARGUMENT_PACKAGE);
            developerPayload = savedInstance.getString(ARGUMENT_DEV_PAYLOAD);
            privateKey64 = savedInstance.getString(ARGUMENT_PRIVATE_KEY);
        } else {
            final Intent intent = getIntent();
            product = intent.getParcelableExtra(ARGUMENT_PRODUCT);
            packageName = intent.getStringExtra(ARGUMENT_PACKAGE);
            developerPayload = intent.getStringExtra(ARGUMENT_DEV_PAYLOAD);
            privateKey64 = intent.getStringExtra(ARGUMENT_PRIVATE_KEY);
        }
        setContentView(R.layout.activity_fake_iabv3_checkout);
        final TextView productName = bind(R.id.product_name);
        final TextView productDescription = bind(R.id.product_description);
        final TextView productPrice = bind(R.id.product_price);
        final Button buyButton = bind(R.id.buy);

        productName.setText(product.name());
        productDescription.setText(product.description());
        productPrice.setText(product.price());
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent data = new Intent();
                try {
                    final String purchaseData = purchaseData();
                    data.putExtra(InAppBillingConstants.RESPONSE_CODE, InAppBillingConstants.BILLING_RESPONSE_RESULT_OK);
                    data.putExtra(InAppBillingConstants.RESPONSE_INAPP_PURCHASE_DATA, purchaseData);
                    data.putExtra(InAppBillingConstants.RESPONSE_INAPP_SIGNATURE, generateSignature(purchaseData, privateKey64));
                    FakeInAppBillingV3Api.addTestPurchase(InAppBillingPurchase.create(product, data));
                } catch (JSONException e) {
                    // Library error, if it happens, promote to RuntimeException
                    throw new RuntimeException(e);
                }

                setResultCompat(Activity.RESULT_OK, data);
                finish();
            }
        });
    }

    private String generateSignature(String purchaseData, String privateKey64) {
        if (TextUtils.isEmpty(privateKey64)) {
            return "TEST_SIGNATURE";
        }

        return Security.sign(privateKey64, purchaseData);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(ARGUMENT_PRODUCT, product);
        savedInstanceState.putString(ARGUMENT_PACKAGE, packageName);
        savedInstanceState.putString(ARGUMENT_DEV_PAYLOAD, developerPayload);
        savedInstanceState.putString(ARGUMENT_PRIVATE_KEY, privateKey64);
    }

    @Override
    public void onBackPressed() {
        final Intent data = new Intent();
        data.putExtra(InAppBillingConstants.RESPONSE_CODE, InAppBillingConstants.BILLING_RESPONSE_RESULT_USER_CANCELED);
        setResultCompat(Activity.RESULT_CANCELED, data);
        finish();
    }

    private String purchaseData() throws JSONException {
        final JSONObject object = new JSONObject();
        object.put(PurchaseConstants.AUTO_RENEWING, product.isSubscription());
        object.put(PurchaseConstants.ORDER_ID, UUID.randomUUID().toString());
        object.put(PurchaseConstants.PACKAGE_NAME, packageName);
        object.put(PurchaseConstants.PRODUCT_ID, product.sku());
        object.put(PurchaseConstants.PURCHASE_TIME, System.currentTimeMillis());
        object.put(PurchaseConstants.PURCHASE_STATE, PurchaseConstants.PURCHASE_STATE_PURCHASED);
        object.put(PurchaseConstants.DEVELOPER_PAYLOAD, developerPayload == null ? "" : developerPayload);
        object.put(PurchaseConstants.PURCHASE_TOKEN, UUID.randomUUID().toString());

        return object.toString();
    }

    private void setResultCompat(int result, Intent data) {
        final Activity parent = getParent();
        if (parent == null) {
            setResult(result, data);
        } else {
            parent.setResult(result, data);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T bind(int id) {
        return (T) findViewById(id);
    }
}
