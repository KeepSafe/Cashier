package com.getkeepsafe.cashier.iab.debug;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class FakeInAppBillingV3ApiTest {
    final String purchaseData = "{\"autoRenewing\":false,\"orderId\":\"7429c5e9-f8e7-4332-b39d-60ce2c215fef\",\"packageName\":\"com.getkeepsafe.cashier.sample\",\"productId\":\"android.test.purchased\",\"purchaseTime\":1476077957823,\"purchaseState\":0,\"developerPayload\":\"hello-cashier!\",\"purchaseToken\":\"15d12f9b-82fc-4977-b49c-aef730a10463\"}";
    @Test
    public void returnsSkuDetails() throws JSONException {
//        Product product = InAppBillingProduct.create(purchaseData, false);
//        FakeInAppBillingV3Api.addTestProduct(product);
//        final FakeInAppBillingV3Api api = new FakeInAppBillingV3Api(mock(Context.class));
//        final Bundle skuDetails =
    }
}
