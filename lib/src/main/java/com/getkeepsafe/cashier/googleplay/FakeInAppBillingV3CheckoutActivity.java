package com.getkeepsafe.cashier.googleplay;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class FakeInAppBillingV3CheckoutActivity extends Activity implements GooglePlayConstants {
    private static final String ARGUMENT_PRODUCT = "product";
    private static final String ARGUMENT_PACKAGE = "package";
    private static final String ARGUMENT_DEV_PAYLOAD = "dev-payload";

    private Product product;
    private String packageName;
    private String developerPayload;

    public static PendingIntent pendingIntent(@NonNull final Context context,
                                              @NonNull final Product product,
                                              @Nullable final String developerPayload) {
        final Intent intent = new Intent(context, FakeInAppBillingV3CheckoutActivity.class);
        intent.putExtra(ARGUMENT_PRODUCT, product);
        intent.putExtra(ARGUMENT_PACKAGE, context.getPackageName());
        if (developerPayload != null) {
            intent.putExtra(ARGUMENT_DEV_PAYLOAD, developerPayload);
        }

        return PendingIntent.getActivity(context, 1337, intent, 0);
    }

    @Override
    protected void onCreate(final Bundle savedInstance) {
        super.onCreate(savedInstance);

        if (savedInstance != null) {
            product = savedInstance.getParcelable(ARGUMENT_PRODUCT);
            packageName = savedInstance.getString(ARGUMENT_PACKAGE);
            developerPayload = savedInstance.getString(ARGUMENT_DEV_PAYLOAD);
        } else {
            final Intent intent = getIntent();
            product = intent.getParcelableExtra(ARGUMENT_PRODUCT);
            packageName = intent.getStringExtra(ARGUMENT_PACKAGE);
            developerPayload = intent.getStringExtra(ARGUMENT_DEV_PAYLOAD);
        }
        setContentView(R.layout.activity_fake_iabv3_checkout);
        final TextView productName = bind(R.id.product_name);
        final TextView productDescription = bind(R.id.product_description);
        final TextView productPrice = bind(R.id.product_price);
        final Button buyButton = bind(R.id.buy);

        productName.setText(product.name);
        productDescription.setText(product.description);
        productPrice.setText(product.price);
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent data = new Intent();
                try {
                    data.putExtra(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
                    data.putExtra(RESPONSE_INAPP_PURCHASE_DATA, purchaseData());
                    data.putExtra(RESPONSE_INAPP_SIGNATURE, "TEST-DATA-SIGNATURE-" + product.sku);
                    FakeInAppBillingV3Api.addTestPurchase(GooglePlayPurchase.of(product, data));
                } catch (JSONException e) {
                    // Library error, if it happens, promote to RuntimeException
                    throw new RuntimeException(e);
                }

                setResultCompat(Activity.RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(ARGUMENT_PRODUCT, product);
        savedInstanceState.putString(ARGUMENT_PACKAGE, packageName);
        savedInstanceState.putString(ARGUMENT_DEV_PAYLOAD, developerPayload);
    }

    @Override
    public void onBackPressed() {
        final Intent data = new Intent();
        data.putExtra(RESPONSE_CODE, BILLING_RESPONSE_RESULT_USER_CANCELED);
        setResultCompat(Activity.RESULT_CANCELED, data);
        finish();
    }

    private String purchaseData() throws JSONException {
        final JSONObject object = new JSONObject();
        object.put(PurchaseConstants.AUTO_RENEWING, product.isSubscription);
        object.put(PurchaseConstants.ORDER_ID, UUID.randomUUID().toString());
        object.put(PurchaseConstants.PACKAGE_NAME, packageName);
        object.put(PurchaseConstants.PRODUCT_ID, product.sku);
        object.put(PurchaseConstants.PURCHASE_TIME, System.currentTimeMillis());
        object.put(PurchaseConstants.PURCHASE_STATE, PurchaseConstants.PURCHASE_STATE_PURCHASED);
        object.put(PurchaseConstants.DEVELOPER_PAYLOAD, developerPayload == null ? "" : developerPayload);
        object.put(PurchaseConstants.PURCHASE_TOKEN, UUID.randomUUID().toString());

        return object.toString();
    }

    private void setResultCompat(final int result, @Nullable final Intent data) {
        if (getParent() == null) {
            setResult(result, data);
        } else {
            getParent().setResult(result, data);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T bind(final int id) {
        return (T) findViewById(id);
    }
}
