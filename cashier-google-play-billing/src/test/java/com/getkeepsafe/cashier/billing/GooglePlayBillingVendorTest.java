package com.getkeepsafe.cashier.billing;

import android.app.Activity;
import android.content.Context;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.getkeepsafe.cashier.ConsumeListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.ProductDetailsListener;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.PurchaseListener;
import com.getkeepsafe.cashier.Vendor;
import com.getkeepsafe.cashier.VendorConstants;
import com.getkeepsafe.cashier.billing.AbstractGooglePlayBillingApi.LifecycleListener;
import com.getkeepsafe.cashier.logging.Logger;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class GooglePlayBillingVendorTest {

    @Mock
    AbstractGooglePlayBillingApi api;

    @Mock
    Context context;

    @Mock
    Activity activity;

    @Mock
    Logger logger;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void initialize_successfully() {
        GooglePlayBillingVendor vendor = new GooglePlayBillingVendor(api);
        vendor.setLogger(logger);
        Vendor.InitializationListener initializationListener = mock(Vendor.InitializationListener.class);

        mockSuccessfulInitialization(vendor);
        when(api.isBillingSupported(BillingClient.SkuType.INAPP)).thenReturn(BillingClient.BillingResponse.OK);
        when(api.isBillingSupported(BillingClient.SkuType.SUBS)).thenReturn(BillingClient.BillingResponse.OK);
        when(api.available()).thenReturn(true);

        vendor.initialize(context, initializationListener);

        verify(api).initialize(eq(context), eq(vendor), eq(vendor), eq(logger));
        verify(initializationListener).initialized();
        assertTrue(vendor.available());
        assertTrue(vendor.canPurchase(TestData.productInappA));
        assertTrue(vendor.canPurchase(TestData.productSubA));
    }

    @Test
    public void initialize_failure() {
        GooglePlayBillingVendor vendor = new GooglePlayBillingVendor(api);
        vendor.setLogger(logger);
        Vendor.InitializationListener initializationListener = mock(Vendor.InitializationListener.class);

        when(api.initialize(eq(context), eq(vendor), eq(vendor), any(Logger.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) {
                LifecycleListener listener = invocation.getArgument(2);
                listener.initialized(false);
                return false;
            }
        });
        when(api.isBillingSupported(BillingClient.SkuType.INAPP)).thenReturn(BillingClient.BillingResponse.OK);
        when(api.isBillingSupported(BillingClient.SkuType.SUBS)).thenReturn(BillingClient.BillingResponse.OK);
        when(api.available()).thenReturn(true);

        vendor.initialize(context, initializationListener);

        verify(api).initialize(eq(context), eq(vendor), eq(vendor), eq(logger));
        verify(initializationListener, never()).initialized();
        verify(initializationListener).unavailable();
        assertFalse(vendor.available());
        assertFalse(vendor.canPurchase(TestData.productInappA));
        assertFalse(vendor.canPurchase(TestData.productSubA));
    }

    @Test
    public void initialize_successfully_when_subs_not_available() {
        GooglePlayBillingVendor vendor = new GooglePlayBillingVendor(api);
        vendor.setLogger(logger);
        Vendor.InitializationListener initializationListener = mock(Vendor.InitializationListener.class);

        mockSuccessfulInitialization(vendor);
        when(api.isBillingSupported(BillingClient.SkuType.INAPP)).thenReturn(BillingClient.BillingResponse.OK);
        when(api.isBillingSupported(BillingClient.SkuType.SUBS)).thenReturn(BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED);
        when(api.available()).thenReturn(true);

        vendor.initialize(context, initializationListener);

        verify(api).initialize(eq(context), eq(vendor), eq(vendor), eq(logger));
        verify(initializationListener).initialized();
        assertTrue(vendor.available());
        assertTrue(vendor.canPurchase(TestData.productInappA));
        assertFalse(vendor.canPurchase(TestData.productSubA));
    }

    @Test
    public void initialize_when_cannot_buy_anything() {
        GooglePlayBillingVendor vendor = new GooglePlayBillingVendor(api);
        vendor.setLogger(logger);
        Vendor.InitializationListener initializationListener = mock(Vendor.InitializationListener.class);

        mockSuccessfulInitialization(vendor);
        when(api.isBillingSupported(BillingClient.SkuType.INAPP)).thenReturn(BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED);
        when(api.isBillingSupported(BillingClient.SkuType.SUBS)).thenReturn(BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED);
        when(api.available()).thenReturn(true);

        vendor.initialize(context, initializationListener);

        verify(api).initialize(eq(context), eq(vendor), eq(vendor), eq(logger));
        verify(initializationListener).initialized();
        assertFalse(vendor.available());
        assertFalse(vendor.canPurchase(TestData.productInappA));
        assertFalse(vendor.canPurchase(TestData.productSubA));
    }

    @Test
    public void do_not_reinitialize() {
        GooglePlayBillingVendor vendor = new GooglePlayBillingVendor(api);
        vendor.setLogger(logger);
        Vendor.InitializationListener initializationListener = mock(Vendor.InitializationListener.class);

        mockSuccessfulInitialization(vendor);
        when(api.isBillingSupported(BillingClient.SkuType.INAPP)).thenReturn(BillingClient.BillingResponse.OK);
        when(api.isBillingSupported(BillingClient.SkuType.SUBS)).thenReturn(BillingClient.BillingResponse.OK);
        when(api.available()).thenReturn(true);

        vendor.initialize(context, initializationListener);
        vendor.initialize(context, initializationListener);
        vendor.initialize(context, initializationListener);

        verify(api, times(1)).initialize(eq(context), eq(vendor), eq(vendor), eq(logger));
    }


    @Test
    public void purchase_successfully() {
        GooglePlayBillingVendor vendor = successfullyInitializedVendor();
        PurchaseListener listener = mock(PurchaseListener.class);
        mockApiPurchaseSuccess(vendor, TestData.productInappA, true);

        vendor.purchase(activity, TestData.productInappA, null, listener);

        ArgumentCaptor<Purchase> argument = ArgumentCaptor.forClass(Purchase.class);
        verify(listener).success(argument.capture());

        assertEquals( TestData.productInappA.sku(), argument.getValue().product().sku() );

        // Should be able to make another purchase now
        vendor.purchase(activity, TestData.productInappA, null, listener);
    }

    @Test
    public void purchase_with_api_error() {
        GooglePlayBillingVendor vendor = successfullyInitializedVendor();
        PurchaseListener listener = mock(PurchaseListener.class);
        mockApiPurchaseFailure(vendor, TestData.productInappA, BillingClient.BillingResponse.SERVICE_UNAVAILABLE);

        vendor.purchase(activity, TestData.productInappA, null, listener);

        ArgumentCaptor<Vendor.Error> argumentError = ArgumentCaptor.forClass(Vendor.Error.class);
        ArgumentCaptor<Product> argumentProduct = ArgumentCaptor.forClass(Product.class);
        verify(listener).failure(argumentProduct.capture(), argumentError.capture());

        assertEquals( TestData.productInappA.sku(), argumentProduct.getValue().sku() );
        assertEquals(VendorConstants.PURCHASE_UNAVAILABLE, argumentError.getValue().code);

        // Should be able to make another purchase now
        vendor.purchase(activity, TestData.productInappA, null, listener);
    }

    @Test
    public void purchase_user_canceled() {
        GooglePlayBillingVendor vendor = successfullyInitializedVendor();
        PurchaseListener listener = mock(PurchaseListener.class);
        mockApiPurchaseFailure(vendor, TestData.productInappA, BillingClient.BillingResponse.USER_CANCELED);

        vendor.purchase(activity, TestData.productInappA, null, listener);

        ArgumentCaptor<Vendor.Error> argumentError = ArgumentCaptor.forClass(Vendor.Error.class);
        ArgumentCaptor<Product> argumentProduct = ArgumentCaptor.forClass(Product.class);
        verify(listener).failure(argumentProduct.capture(), argumentError.capture());

        assertEquals( TestData.productInappA.sku(), argumentProduct.getValue().sku() );
        assertEquals(VendorConstants.PURCHASE_CANCELED, argumentError.getValue().code);

        // Should be able to make another purchase now
        vendor.purchase(activity, TestData.productInappA, null, listener);
    }

    @Test
    public void purchase_with_signature_error() {
        GooglePlayBillingVendor vendor = successfullyInitializedVendor();
        PurchaseListener listener = mock(PurchaseListener.class);
        mockApiPurchaseSuccess(vendor, TestData.productInappA, false);

        vendor.purchase(activity, TestData.productInappA, null, listener);

        ArgumentCaptor<Vendor.Error> argumentError = ArgumentCaptor.forClass(Vendor.Error.class);
        ArgumentCaptor<Product> argumentProduct = ArgumentCaptor.forClass(Product.class);
        verify(listener).failure(argumentProduct.capture(), argumentError.capture());

        assertEquals( TestData.productInappA.sku(), argumentProduct.getValue().sku() );
        assertEquals(VendorConstants.PURCHASE_SUCCESS_RESULT_MALFORMED, argumentError.getValue().code);

        // Should be able to make another purchase now
        vendor.purchase(activity, TestData.productInappA, null, listener);
    }

    @Test(expected = RuntimeException.class)
    public void purchase_while_another_purchase_in_progress() {
        GooglePlayBillingVendor vendor = successfullyInitializedVendor();
        PurchaseListener listener = mock(PurchaseListener.class);

        vendor.purchase(activity, TestData.productInappA, null, listener);

        vendor.purchase(activity, TestData.productInappB, null, listener);
    }

    @Test(expected = RuntimeException.class)
    public void purchase_developer_payload_not_supported() {
        GooglePlayBillingVendor vendor = successfullyInitializedVendor();
        PurchaseListener listener = mock(PurchaseListener.class);
        mockApiPurchaseSuccess(vendor, TestData.productInappA, true);

        vendor.purchase(activity, TestData.productInappA, "DEV PAYLOAD", listener);
    }

    @Test
    public void consume() throws JSONException {
        GooglePlayBillingVendor vendor = successfullyInitializedVendor();
        ConsumeListener listener = mock(ConsumeListener.class);
        Purchase purchase = GooglePlayBillingPurchase.create(TestData.productInappA, new TestPurchase(TestData.productInappA));
        mockConsume(vendor, BillingClient.BillingResponse.OK);

        vendor.consume(context, purchase, listener);

        verify(listener).success(purchase);
    }

    @Test()
    public void cannot_consume_twice() throws JSONException {
        GooglePlayBillingVendor vendor = successfullyInitializedVendor();
        ConsumeListener listener = mock(ConsumeListener.class);
        Purchase purchase = GooglePlayBillingPurchase.create(TestData.productInappA, new TestPurchase(TestData.productInappA));
        mockConsume(vendor, BillingClient.BillingResponse.OK);

        vendor.consume(context, purchase, listener);
        verify(listener).success(purchase);

        vendor.consume(context, purchase, listener);
        ArgumentCaptor<Vendor.Error> argumentError = ArgumentCaptor.forClass(Vendor.Error.class);
        verify(listener).failure(eq(purchase), argumentError.capture());
        assertEquals(VendorConstants.CONSUME_UNAVAILABLE, argumentError.getValue().code);
    }

    @Test
    public void consume_error() throws JSONException {
        GooglePlayBillingVendor vendor = successfullyInitializedVendor();
        ConsumeListener listener = mock(ConsumeListener.class);
        Purchase purchase = GooglePlayBillingPurchase.create(TestData.productInappA, new TestPurchase(TestData.productInappA));
        mockConsume(vendor, BillingClient.BillingResponse.ITEM_NOT_OWNED);

        vendor.consume(context, purchase, listener);

        ArgumentCaptor<Vendor.Error> argumentError = ArgumentCaptor.forClass(Vendor.Error.class);
        verify(listener).failure(eq(purchase), argumentError.capture());
        assertEquals(VendorConstants.CONSUME_NOT_OWNED, argumentError.getValue().code);
    }

    @Test
    public void get_product_details() {
        GooglePlayBillingVendor vendor = successfullyInitializedVendor();
        ProductDetailsListener listener = mock(ProductDetailsListener.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                List<String> skus = invocation.getArgument(1);
                SkuDetailsResponseListener responseListener = invocation.getArgument(2);

                assertEquals(1, skus.size());

                responseListener.onSkuDetailsResponse(BillingClient.BillingResponse.OK,
                        Collections.singletonList( TestData.getSkuDetail(skus.get(0)) ));
                return null;
            }
        }).when(api).getSkuDetails(eq(BillingClient.SkuType.INAPP), ArgumentMatchers.<String>anyList(), any(SkuDetailsResponseListener.class));

        vendor.getProductDetails(context, TestData.productInappA.sku(), false, listener);

        ArgumentCaptor<List<String>> argumentSkus = ArgumentCaptor.forClass(List.class);
        verify(api).getSkuDetails(eq(BillingClient.SkuType.INAPP), argumentSkus.capture(), any(SkuDetailsResponseListener.class));
        assertEquals(1, argumentSkus.getValue().size());
        assertEquals(TestData.productInappA.sku(), argumentSkus.getValue().get(0));

        ArgumentCaptor<Product> argumentProduct = ArgumentCaptor.forClass(Product.class);
        verify(listener).success(argumentProduct.capture());
        assertEquals(TestData.productInappA, argumentProduct.getValue());
    }

    @Test
    public void get_product_details_failure() {
        GooglePlayBillingVendor vendor = successfullyInitializedVendor();
        ProductDetailsListener listener = mock(ProductDetailsListener.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                List<String> skus = invocation.getArgument(1);
                SkuDetailsResponseListener responseListener = invocation.getArgument(2);

                assertEquals(1, skus.size());

                responseListener.onSkuDetailsResponse(BillingClient.BillingResponse.SERVICE_UNAVAILABLE, null);
                return null;
            }
        }).when(api).getSkuDetails(eq(BillingClient.SkuType.INAPP), ArgumentMatchers.<String>anyList(), any(SkuDetailsResponseListener.class));

        vendor.getProductDetails(context, TestData.productInappA.sku(), false, listener);

        ArgumentCaptor<Vendor.Error> argumentError = ArgumentCaptor.forClass(Vendor.Error.class);
        verify(listener).failure(argumentError.capture());
        assertEquals(VendorConstants.PRODUCT_DETAILS_UNAVAILABLE, argumentError.getValue().code);
    }


    private void mockSuccessfulInitialization(GooglePlayBillingVendor vendor) {
        when(api.initialize(eq(context), eq(vendor), eq(vendor), any(Logger.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) {
                LifecycleListener listener = invocation.getArgument(2);
                listener.initialized(true);
                return true;
            }
        });
    }

    private void mockApiPurchaseSuccess(final GooglePlayBillingVendor vendor, final Product product, final boolean validSignature) {
        doAnswer(
                new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        vendor.onPurchasesUpdated(BillingClient.BillingResponse.OK,
                                Collections.singletonList(
                                        validSignature ? new TestPurchase(product) : new TestPurchase(product, "INVALID")
                                )
                        );
                        return null;
                    }
                }
        ).when(api).launchBillingFlow(activity, product.sku(), BillingClient.SkuType.INAPP);
    }

    private void mockApiPurchaseFailure(final GooglePlayBillingVendor vendor, final Product product, final int responseCode) {
        doAnswer(
                new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        vendor.onPurchasesUpdated(responseCode, null);
                        return null;
                    }
                }
        ).when(api).launchBillingFlow(activity, product.sku(), BillingClient.SkuType.INAPP);
    }

    private void mockConsume(final GooglePlayBillingVendor vendor, final int responseCode) {
        doAnswer(
                new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        String token = invocation.getArgument(0);
                        ConsumeResponseListener listener = invocation.getArgument(1);
                        listener.onConsumeResponse(responseCode, token);
                        return null;
                    }
                }
        ).when(api).consumePurchase(anyString(), any(ConsumeResponseListener.class));
    }

    GooglePlayBillingVendor successfullyInitializedVendor() {
        GooglePlayBillingVendor vendor = new GooglePlayBillingVendor(api, TestData.TEST_PUBLIC_KEY);
        vendor.setLogger(logger);
        Vendor.InitializationListener initializationListener = mock(Vendor.InitializationListener.class);

        when(api.initialize(eq(context), eq(vendor), eq(vendor), any(Logger.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) {
                LifecycleListener listener = invocation.getArgument(2);
                listener.initialized(true);
                return true;
            }
        });
        when(api.isBillingSupported(BillingClient.SkuType.INAPP)).thenReturn(BillingClient.BillingResponse.OK);
        when(api.isBillingSupported(BillingClient.SkuType.SUBS)).thenReturn(BillingClient.BillingResponse.OK);
        when(api.available()).thenReturn(true);

        vendor.initialize(context, initializationListener);

        return vendor;
    }
}
