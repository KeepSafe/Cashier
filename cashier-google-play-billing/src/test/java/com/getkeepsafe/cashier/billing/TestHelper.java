package com.getkeepsafe.cashier.billing;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.getkeepsafe.cashier.Product;

import org.json.JSONException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestHelper {

    static Threading mockThreading() {
        Threading mock = mock(Threading.class);
        Answer executeAnswer = new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = invocation.getArgument(0);
                runnable.run();
                return null;
            }
        };
        doAnswer(executeAnswer).when(mock).runOnMainThread(any(Runnable.class));
        doAnswer(executeAnswer).when(mock).runInBackground(any(Runnable.class));
        return mock;
    }

    static void mockSkuDetails(AbstractGooglePlayBillingApi api, String type, final Map<String, SkuDetails> skuDetailsMap) {
        doAnswer(
                new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        List<String> skus = invocation.getArgument(1);
                        SkuDetailsResponseListener listener = invocation.getArgument(2);
                        List<SkuDetails> result = new ArrayList<>();
                        for (String sku : skus) {
                            result.add(skuDetailsMap.get(sku));
                        }
                        listener.onSkuDetailsResponse(BillingClient.BillingResponse.OK, result);
                        return null;
                    }
                }
        ).when(api).getSkuDetails(
                eq(type),
                Mockito.<String>anyList(),
                any(SkuDetailsResponseListener.class));
    }

    static void mockSkuDetailsError(AbstractGooglePlayBillingApi api, String type) {
        doAnswer(
                new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        SkuDetailsResponseListener listener = invocation.getArgument(2);
                        listener.onSkuDetailsResponse(BillingClient.BillingResponse.SERVICE_UNAVAILABLE, null);
                        return null;
                    }
                }
        ).when(api).getSkuDetails(
                eq(type),
                Mockito.<String>anyList(),
                any(SkuDetailsResponseListener.class));
    }

    static void mockApiUnavailable(AbstractGooglePlayBillingApi api) {
        doAnswer(
                new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        throw new IllegalStateException("Billing client is not available");
                    }
                }
        ).when(api).getSkuDetails(
                anyString(),
                Mockito.<String>anyList(),
                any(SkuDetailsResponseListener.class));

        when(api.available()).thenReturn(false);
        when(api.getPurchases(anyString())).thenThrow(new IllegalStateException("Billing client is not available"));
    }

    static void mockPurchases(AbstractGooglePlayBillingApi api, final List<Product> products) {
        List<Purchase> purchases = new ArrayList<>();
        List<Purchase> inapp = new ArrayList<>();
        List<Purchase> subs = new ArrayList<>();
        for (Product product : products) {
            try {
                Purchase purchase = new TestPurchase(product);
                purchases.add(purchase);
                if (product.isSubscription()) {
                    subs.add(purchase);
                } else {
                    inapp.add(purchase);
                }
            } catch (JSONException e) {}
        }
        when(api.getPurchases()).thenReturn(purchases);
        when(api.getPurchases(BillingClient.SkuType.INAPP)).thenReturn(inapp);
        when(api.getPurchases(BillingClient.SkuType.SUBS)).thenReturn(subs);
    }
}
