package com.getkeepsafe.cashier.iab;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.RemoteException;

import com.getkeepsafe.cashier.ConsumeListener;
import com.getkeepsafe.cashier.Inventory;
import com.getkeepsafe.cashier.InventoryListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.ProductDetailsListener;
import com.getkeepsafe.cashier.Purchase;
import com.getkeepsafe.cashier.PurchaseListener;
import com.getkeepsafe.cashier.Vendor;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static com.getkeepsafe.cashier.VendorConstants.CONSUME_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.CONSUME_UNAVAILABLE;
import static com.getkeepsafe.cashier.VendorConstants.INVENTORY_QUERY_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.INVENTORY_QUERY_MALFORMED_RESPONSE;
import static com.getkeepsafe.cashier.VendorConstants.PRODUCT_DETAILS_NOT_FOUND;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_FAILURE;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_SUCCESS_RESULT_MALFORMED;
import static com.getkeepsafe.cashier.VendorConstants.PURCHASE_UNAVAILABLE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_ERROR;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.BILLING_RESPONSE_RESULT_OK;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_BUY_INTENT;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_CODE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_GET_SKU_DETAILS_LIST;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_ITEM_LIST;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_PURCHASE_DATA;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_PURCHASE_DATA_LIST;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_SIGNATURE;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.RESPONSE_INAPP_SIGNATURE_LIST;
import static com.getkeepsafe.cashier.iab.InAppBillingConstants.VENDOR_PACKAGE;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.IN_APP_BILLING_PRODUCT_VALID_PRODUCT_JSON;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.TEST_PUBLIC_KEY;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.TEST_INVALID_PUBLIC_KEY;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.VALID_ONE_TIME_PURCHASE_JSON;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.VALID_PURCHASE_RECEIPT_JSON;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.VALID_SUBSCRIPTION_PURCHASE_JSON;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class InAppBillingV3VendorTest {

    @Mock
    private Vendor.InitializationListener initializationListener;
    @Mock
    private AbstractInAppBillingV3API api;
    @Mock
    private Activity activity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void passesInitializationWithWorkingApi() throws RemoteException {
        initialMockApi(true);
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);
        verify(initializationListener, times(1)).initialized();
        verify(initializationListener, never()).unavailable();
    }

    @Test
    public void failsInitializationWithBrokenApi() throws RemoteException {
        initialMockApi(false);
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);
        verify(initializationListener, times(1)).unavailable();
        verify(initializationListener, never()).initialized();
    }

    @Test
    public void canPurchaseAfterSuccessfulInitialization() throws RemoteException {
        checkPurchaseAfterInitialization(true);
    }

    @Test
    public void canNotPurchaseAfterFailedInitialization() throws RemoteException {
        checkPurchaseAfterInitialization(false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void purchaseRequiresActivity() throws RemoteException {
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.purchase(null, mock(Product.class), null, mock(PurchaseListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void purchaseRequiresProduct() throws RemoteException {
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.purchase(mock(Activity.class), null, null, mock(PurchaseListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void purchaseRequiresListener() throws RemoteException {
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.purchase(mock(Activity.class), mock(Product.class), null, null);
    }

    @Test(expected = IllegalStateException.class)
    public void purchaseRequiresInitialization() throws RemoteException {
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.purchase(mock(Activity.class), mock(Product.class), null, mock(PurchaseListener.class));
    }

    @Test(expected = IllegalStateException.class)
    public void purchaseRequiresSuccessfulInitialization() throws RemoteException {
        initialMockApi(false);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        Product inappPurchase = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "1",
                        "1",
                        "1",
                        "1",
                        "1",
                        false,
                        1);

        vendor.purchase(mock(Activity.class), inappPurchase, null, mock(PurchaseListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void purchaseThrowsIfInitializedButBillingNotSupported() throws RemoteException {
        initialMockApi(true);
        when(api.isBillingSupported(anyString())).thenReturn(BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        Product inappPurchase = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "1",
                        "1",
                        "1",
                        "1",
                        "1",
                        false,
                        1);

        vendor.purchase(mock(Activity.class), inappPurchase, null, mock(PurchaseListener.class));
    }

    @Test
    public void purchaseFailsIfBuyBundleIsInvalid() throws RemoteException {
        final Bundle bundle = mock(Bundle.class);
        when(bundle.get(RESPONSE_CODE)).thenReturn(BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE);

        initialMockApi(true);
        when(api.getBuyIntent(anyString(), anyString(), or(isNull(String.class), anyString()))).thenReturn(bundle);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        Product inappPurchase = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "1",
                        "1",
                        "1",
                        "1",
                        "1",
                        false,
                        1);

        PurchaseListener purchaseListener = mock(PurchaseListener.class);
        vendor.purchase(mock(Activity.class), inappPurchase, null, purchaseListener);
        verify(purchaseListener, times(1)).failure(inappPurchase, new Vendor.Error(PURCHASE_UNAVAILABLE, BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE));
    }

    @Test
    public void purchaseFailsIfBuyIntentIsNull() throws RemoteException {
        final Bundle bundle = mock(Bundle.class);
        when(bundle.get(RESPONSE_CODE)).thenReturn(BILLING_RESPONSE_RESULT_OK);
        when(bundle.getParcelable(RESPONSE_BUY_INTENT)).thenReturn(null);

        initialMockApi(true);
        when(api.getBuyIntent(anyString(), anyString(), or(isNull(String.class), anyString()))).thenReturn(bundle);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        Product inappPurchase = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "1",
                        "1",
                        "1",
                        "1",
                        "1",
                        false,
                        1);

        PurchaseListener purchaseListener = mock(PurchaseListener.class);
        vendor.purchase(mock(Activity.class), inappPurchase, null, purchaseListener);
        verify(purchaseListener, times(1)).failure(inappPurchase, new Vendor.Error(PURCHASE_FAILURE, BILLING_RESPONSE_RESULT_OK));
    }

    @Test
    public void purchaseStartsIfParametersAndBundleOk() throws RemoteException, IntentSender.SendIntentException {
        mockDependeniesForSuccessfulPurchaseFlow();

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        Product inappPurchase = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "1",
                        "1",
                        "1",
                        "1",
                        "1",
                        false,
                        1);

        PurchaseListener purchaseListener = mock(PurchaseListener.class);
        vendor.purchase(activity, inappPurchase, null, purchaseListener);
        vendor.onActivityResult(vendor.getRequestCode(), Activity.RESULT_OK, null);
        verify(activity, times(1)).startIntentSenderForResult(any(IntentSender.class), anyInt(), any(Intent.class), anyInt(), anyInt(), anyInt());
    }

    @Test
    public void purchaseFailsIfResultIntentIsNull() throws RemoteException, IntentSender.SendIntentException {
        mockDependeniesForSuccessfulPurchaseFlow();

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        Product inappPurchase = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "1",
                        "1",
                        "1",
                        "1",
                        "1",
                        false,
                        1);

        PurchaseListener purchaseListener = mock(PurchaseListener.class);
        vendor.purchase(activity, inappPurchase, null, purchaseListener);
        vendor.onActivityResult(vendor.getRequestCode(), Activity.RESULT_OK, null);
        verify(purchaseListener, times(1)).failure(inappPurchase, new Vendor.Error(PURCHASE_FAILURE, BILLING_RESPONSE_RESULT_ERROR));
    }

    @Test
    public void purchaseFailsIfDevPayloadIsInvalid() throws RemoteException, IntentSender.SendIntentException {
        mockDependeniesForSuccessfulPurchaseFlow();

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        Product inappPurchase = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "so.product.much.purchase",
                        "1",
                        "1",
                        "1",
                        "1",
                        false,
                        1);

        PurchaseListener purchaseListener = mock(PurchaseListener.class);
        vendor.purchase(activity, inappPurchase, null, purchaseListener);

        vendor.onActivityResult(
                vendor.getRequestCode(),
                Activity.RESULT_OK,
                new Intent()
                        .putExtra(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK)
                        .putExtra(RESPONSE_INAPP_PURCHASE_DATA, VALID_PURCHASE_RECEIPT_JSON)
                        .putExtra(RESPONSE_INAPP_SIGNATURE, "1"));

        verify(purchaseListener, times(1)).failure(inappPurchase, new Vendor.Error(PURCHASE_SUCCESS_RESULT_MALFORMED, BILLING_RESPONSE_RESULT_ERROR));
    }

    @Test
    public void purchaseFailsIfSignatureIsInvalid() throws RemoteException, IntentSender.SendIntentException {
        mockDependeniesForSuccessfulPurchaseFlow();

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, TEST_PUBLIC_KEY);
        vendor.initialize(mock(Context.class), initializationListener);

        Product inappPurchase = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "so.product.much.purchase",
                        "1",
                        "1",
                        "1",
                        "1",
                        false,
                        1);

        PurchaseListener purchaseListener = mock(PurchaseListener.class);
        vendor.purchase(activity, inappPurchase, "hello-cashier!", purchaseListener);

        vendor.onActivityResult(
                vendor.getRequestCode(),
                Activity.RESULT_OK,
                new Intent()
                        .putExtra(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK)
                        .putExtra(RESPONSE_INAPP_PURCHASE_DATA, VALID_PURCHASE_RECEIPT_JSON)
                        .putExtra(RESPONSE_INAPP_SIGNATURE, TEST_INVALID_PUBLIC_KEY));

        verify(purchaseListener, times(1)).failure(inappPurchase, new Vendor.Error(PURCHASE_SUCCESS_RESULT_MALFORMED, BILLING_RESPONSE_RESULT_ERROR));
    }

    @Test
    public void purchaseFailsIfResponseCodeIsInvalid() throws RemoteException, IntentSender.SendIntentException, JSONException {
        mockDependeniesForSuccessfulPurchaseFlow();

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        Product inappPurchase = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "so.product.much.purchase",
                        "1",
                        "1",
                        "1",
                        "1",
                        false,
                        1);

        PurchaseListener purchaseListener = mock(PurchaseListener.class);
        vendor.purchase(activity, inappPurchase, null, purchaseListener);

        Intent intent = new Intent()
                .putExtra(RESPONSE_CODE, BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE)
                .putExtra(RESPONSE_INAPP_PURCHASE_DATA, VALID_PURCHASE_RECEIPT_JSON)
                .putExtra(RESPONSE_INAPP_SIGNATURE, "");

        vendor.onActivityResult(
                vendor.getRequestCode(),
                Activity.RESULT_OK,
                intent);

        verify(purchaseListener, times(1)).failure(inappPurchase, new Vendor.Error(PURCHASE_UNAVAILABLE, BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE));
    }

    @Test
    public void purchaseFailsIfResultCodeIsInvalid() throws RemoteException, IntentSender.SendIntentException, JSONException {
        mockDependeniesForSuccessfulPurchaseFlow();

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        Product inappPurchase = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "so.product.much.purchase",
                        "1",
                        "1",
                        "1",
                        "1",
                        false,
                        1);

        PurchaseListener purchaseListener = mock(PurchaseListener.class);
        vendor.purchase(activity, inappPurchase, null, purchaseListener);

        Intent intent = new Intent()
                .putExtra(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK)
                .putExtra(RESPONSE_INAPP_PURCHASE_DATA, VALID_PURCHASE_RECEIPT_JSON)
                .putExtra(RESPONSE_INAPP_SIGNATURE, "");

        vendor.onActivityResult(
                vendor.getRequestCode(),
                Activity.RESULT_CANCELED,
                intent);

        verify(purchaseListener, times(1)).failure(inappPurchase, new Vendor.Error(PURCHASE_FAILURE, BILLING_RESPONSE_RESULT_OK));
    }

    @Test
    public void purchaseFailsIfRequestCodeIsInvalid() throws RemoteException, IntentSender.SendIntentException, JSONException {
        mockDependeniesForSuccessfulPurchaseFlow();

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        Product inappPurchase = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "so.product.much.purchase",
                        "1",
                        "1",
                        "1",
                        "1",
                        false,
                        1);

        PurchaseListener purchaseListener = mock(PurchaseListener.class);
        vendor.purchase(activity, inappPurchase, null, purchaseListener);

        Intent intent = new Intent()
                .putExtra(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK)
                .putExtra(RESPONSE_INAPP_PURCHASE_DATA, VALID_PURCHASE_RECEIPT_JSON)
                .putExtra(RESPONSE_INAPP_SIGNATURE, "");

        boolean result = vendor.onActivityResult(
                vendor.getRequestCode() + 1,
                Activity.RESULT_CANCELED,
                intent);

        assertFalse(result);
        verify(purchaseListener, never()).failure(any(Product.class), any(Vendor.Error.class));
        verify(purchaseListener, never()).success(any(Purchase.class));
    }

    @Test
    public void ifPurchaseSuccessfulThenNotifiesListener() throws RemoteException, IntentSender.SendIntentException, JSONException {
        mockDependeniesForSuccessfulPurchaseFlow();

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        Product inappPurchase = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "so.product.much.purchase",
                        "1",
                        "1",
                        "1",
                        "1",
                        false,
                        1);

        PurchaseListener purchaseListener = mock(PurchaseListener.class);
        vendor.purchase(activity, inappPurchase, "hello-cashier!", purchaseListener);

        Intent intent = new Intent()
                .putExtra(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK)
                .putExtra(RESPONSE_INAPP_PURCHASE_DATA, VALID_PURCHASE_RECEIPT_JSON)
                .putExtra(RESPONSE_INAPP_SIGNATURE, TEST_PUBLIC_KEY);

        vendor.onActivityResult(
                vendor.getRequestCode(),
                Activity.RESULT_OK,
                intent);

        verify(purchaseListener, times(1)).success(InAppBillingPurchase.create(inappPurchase, intent));
    }

    @Test(expected = IllegalArgumentException.class)
    public void consumeRequiresContext() throws RemoteException {
        initialMockApi(true);
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);
        vendor.consume(null, mock(Purchase.class), mock(ConsumeListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void consumeRequiresPurchase() throws RemoteException {
        initialMockApi(true);
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);
        vendor.consume(mock(Context.class), null, mock(ConsumeListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void consumeRequiresListener() throws RemoteException {
        initialMockApi(true);
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);
        vendor.consume(mock(Context.class), mock(Purchase.class), null);
    }

    @Test(expected = IllegalStateException.class)
    public void consumeRequiresInitialization() throws RemoteException {
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.consume(mock(Context.class), mock(Purchase.class), mock(ConsumeListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void consumeThrowsIfSubscriptionProvided() throws RemoteException, JSONException {
        initialMockApi(true);
        InAppBillingPurchase inAppBillingPurchase = InAppBillingPurchase.create(VALID_SUBSCRIPTION_PURCHASE_JSON);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);
        vendor.consume(mock(Context.class), inAppBillingPurchase, mock(ConsumeListener.class));
    }

    @Test
    public void consumeFailsIfApiReturnsErrorCode() throws RemoteException, JSONException {
        initialMockApi(true);
        when(api.consumePurchase(anyString())).thenReturn(BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE);

        ConsumeListener listener = mock(ConsumeListener.class);

        InAppBillingPurchase inAppBillingPurchase = InAppBillingPurchase.create(VALID_ONE_TIME_PURCHASE_JSON);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);
        vendor.consume(mock(Context.class), inAppBillingPurchase.purchase(), listener);

        verify(listener, times(1)).failure(inAppBillingPurchase.purchase(), new Vendor.Error(CONSUME_UNAVAILABLE, BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE));
        verify(listener, never()).success(any(Purchase.class));
    }

    @Test
    public void consumeFailsIfApiThrowsRemoteException() throws RemoteException, JSONException {
        initialMockApi(true);
        when(api.consumePurchase(anyString())).thenThrow(RemoteException.class);

        ConsumeListener listener = mock(ConsumeListener.class);

        InAppBillingPurchase inAppBillingPurchase = InAppBillingPurchase.create(VALID_ONE_TIME_PURCHASE_JSON);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);
        vendor.consume(mock(Context.class), inAppBillingPurchase.purchase(), listener);

        verify(listener, times(1)).failure(inAppBillingPurchase.purchase(), new Vendor.Error(CONSUME_FAILURE, BILLING_RESPONSE_RESULT_ERROR));
        verify(listener, never()).success(any(Purchase.class));
    }

    @Test
    public void ifConsumeSuccessfulThenNotifiesListener() throws RemoteException, JSONException {
        initialMockApi(true);

        ConsumeListener listener = mock(ConsumeListener.class);

        InAppBillingPurchase inAppBillingPurchase = InAppBillingPurchase.create(VALID_ONE_TIME_PURCHASE_JSON);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);
        vendor.consume(mock(Context.class), inAppBillingPurchase.purchase(), listener);

        verify(listener, times(1)).success(inAppBillingPurchase.purchase());
        verify(listener, never()).failure(any(Purchase.class), any(Vendor.Error.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getInventoryRequiresContext() throws RemoteException {
        initialMockApi(true);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        vendor.getInventory(null, null, null, mock(InventoryListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getInventoryRequiresListener() throws RemoteException {
        initialMockApi(true);
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        vendor.getInventory(mock(Context.class), null, null, null);
    }

    @Test(expected = IllegalStateException.class)
    public void getInventoryRequiresInitialization() throws RemoteException {
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.getInventory(mock(Context.class), null, null, mock(InventoryListener.class));
    }

    @Test
    public void getInventoryFailsIfApiReturnsErrorCode() throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE);

        initialMockApi(true);
        when(api.getPurchases(anyString(), or(isNull(String.class), anyString()))).thenReturn(bundle);
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        InventoryListener listener = mock(InventoryListener.class);
        vendor.getInventory(mock(Context.class), null, null, listener);

        verify(listener, times(1)).failure(new Vendor.Error(INVENTORY_QUERY_FAILURE, BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE));
        verify(listener, never()).success(any(Inventory.class));
    }

    @Test
    public void getInventoryFailsIfApiThrowsRemoteException() throws RemoteException, JSONException {
        initialMockApi(true);
        when(api.getPurchases(anyString(), or(isNull(String.class), anyString()))).thenThrow(RemoteException.class);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        InventoryListener listener = mock(InventoryListener.class);
        vendor.getInventory(mock(Context.class), null, null, listener);
        verify(listener, times(1)).failure(new Vendor.Error(INVENTORY_QUERY_FAILURE, -1));
        verify(listener, never()).success(any(Inventory.class));
    }

    @Test
    public void getInventoryFailsWhenPurchasesBundleDataIsEmpty() throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);

        initialMockApi(true);
        when(api.getPurchases(anyString(), or(isNull(String.class), anyString()))).thenReturn(bundle);
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        InventoryListener listener = mock(InventoryListener.class);
        vendor.getInventory(mock(Context.class), null, null, listener);
        verify(listener, times(1)).failure(new Vendor.Error(INVENTORY_QUERY_FAILURE, BILLING_RESPONSE_RESULT_ERROR));
        verify(listener, never()).success(any(Inventory.class));
    }

    @Test
    public void getInventoryFailsWhenProcessingBundleInvalidJson() throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
        bundle.putStringArrayList(RESPONSE_INAPP_ITEM_LIST, new ArrayList<String>() {{
            add("so.product.much.purchase");
        }});
        bundle.putStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST, new ArrayList<String>() {{
            add("bad json");
        }});
        bundle.putStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST, new ArrayList<String>() {{
            add("");
        }});

        Bundle skuDetailsBundle = new Bundle();
        skuDetailsBundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
        skuDetailsBundle.putStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST, new ArrayList<String>() {{
            add(IN_APP_BILLING_PRODUCT_VALID_PRODUCT_JSON);
        }});

        initialMockApi(true);
        when(api.getPurchases(anyString(), or(isNull(String.class), anyString()))).thenReturn(bundle);
        when(api.getSkuDetails(anyString(), any(Bundle.class))).thenReturn(skuDetailsBundle);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        InventoryListener listener = mock(InventoryListener.class);
        vendor.getInventory(mock(Context.class), null, null, listener);
        verify(listener, times(1)).failure(new Vendor.Error(INVENTORY_QUERY_MALFORMED_RESPONSE, -1));
        verify(listener, never()).success(any(Inventory.class));
    }

    @Test
    public void ifGetInventorySuccessfulWithNoPurchasesThenNotifiesListener() throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
        bundle.putStringArrayList(RESPONSE_INAPP_ITEM_LIST, new ArrayList<String>());
        bundle.putStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST, new ArrayList<String>());
        bundle.putStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST, new ArrayList<String>());

        initialMockApi(true);
        when(api.getPurchases(anyString(), or(isNull(String.class), anyString()))).thenReturn(bundle);
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        InventoryListener listener = mock(InventoryListener.class);
        vendor.getInventory(mock(Context.class), null, null, listener);
        ArgumentCaptor<Inventory> argumentCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(listener, times(1)).success(argumentCaptor.capture());
        verify(listener, never()).failure(any(Vendor.Error.class));
        assertThat(argumentCaptor.getValue().products().size()).isEqualTo(0);
        assertThat(argumentCaptor.getValue().purchases().size()).isEqualTo(0);
    }

    @Test
    public void ifGetInventoryPurchasesOnlyThenNotifiesListener() throws RemoteException {
        Bundle purchaseBundle = new Bundle();
        purchaseBundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
        purchaseBundle.putStringArrayList(RESPONSE_INAPP_ITEM_LIST, new ArrayList<String>() {{
            add("so.product.much.purchase");
        }});
        purchaseBundle.putStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST, new ArrayList<String>() {{
            add(VALID_PURCHASE_RECEIPT_JSON);
        }});
        purchaseBundle.putStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST, new ArrayList<String>() {{
            add("");
        }});

        Bundle skuDetailsBundle = new Bundle();
        skuDetailsBundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
        skuDetailsBundle.putStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST, new ArrayList<String>() {{
            add(IN_APP_BILLING_PRODUCT_VALID_PRODUCT_JSON);
        }});

        initialMockApi(true);

        when(api.getPurchases(anyString(), or(isNull(String.class), anyString()))).thenReturn(purchaseBundle);
        when(api.getSkuDetails(anyString(), any(Bundle.class))).thenReturn(skuDetailsBundle);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        InventoryListener listener = mock(InventoryListener.class);
        vendor.getInventory(mock(Context.class), null, null, listener);
        ArgumentCaptor<Inventory> argumentCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(listener, times(1)).success(argumentCaptor.capture());
        verify(listener, never()).failure(any(Vendor.Error.class));
        assertThat(argumentCaptor.getValue().products().size()).isEqualTo(0);
        assertThat(argumentCaptor.getValue().purchases().size()).isEqualTo(2);
        assertFalse(argumentCaptor.getValue().purchases().get(0).product().isSubscription());
        assertTrue(argumentCaptor.getValue().purchases().get(1).product().isSubscription());
    }

    @Test
    public void getInventoryReturnsPurchasesAndSpecifiedProducts() throws RemoteException {
        Bundle purchaseBundle = new Bundle();
        purchaseBundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
        purchaseBundle.putStringArrayList(RESPONSE_INAPP_ITEM_LIST, new ArrayList<String>() {{
            add("so.product.much.purchase");
        }});
        purchaseBundle.putStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST, new ArrayList<String>() {{
            add(VALID_PURCHASE_RECEIPT_JSON);
        }});
        purchaseBundle.putStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST, new ArrayList<String>() {{
            add("");
        }});

        Bundle skuDetailsBundle = new Bundle();
        skuDetailsBundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
        skuDetailsBundle.putStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST, new ArrayList<String>() {{
            add(IN_APP_BILLING_PRODUCT_VALID_PRODUCT_JSON);
        }});

        initialMockApi(true);

        when(api.getPurchases(anyString(), or(isNull(String.class), anyString()))).thenReturn(purchaseBundle);
        when(api.getSkuDetails(anyString(), any(Bundle.class))).thenReturn(skuDetailsBundle);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        InventoryListener listener = mock(InventoryListener.class);

        vendor.getInventory(
                mock(Context.class),
                new ArrayList<String>() {{
                    add("so.product.much.purchase");
                }},
                new ArrayList<String>() {{
                    add("so.product.much.purchase");
                }},
                listener);

        ArgumentCaptor<Inventory> argumentCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(listener, times(1)).success(argumentCaptor.capture());
        verify(listener, never()).failure(any(Vendor.Error.class));
        assertThat(argumentCaptor.getValue().products().size()).isEqualTo(2);
        assertThat(argumentCaptor.getValue().purchases().size()).isEqualTo(2);
        assertFalse(argumentCaptor.getValue().products().get(0).isSubscription());
        assertTrue(argumentCaptor.getValue().products().get(1).isSubscription());
        assertFalse(argumentCaptor.getValue().purchases().get(0).product().isSubscription());
        assertTrue(argumentCaptor.getValue().purchases().get(1).product().isSubscription());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProductDetailsRequiresContext() throws RemoteException {
        initialMockApi(true);
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);
        vendor.getProductDetails(null, "", false, mock(ProductDetailsListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProductDetailsRequiresSku() throws RemoteException {
        initialMockApi(true);
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);
        vendor.getProductDetails(mock(Context.class), null, false, mock(ProductDetailsListener.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProductDetailsRequiresListener() throws RemoteException {
        initialMockApi(true);
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);
        vendor.getProductDetails(mock(Context.class), "", false, null);
    }

    @Test(expected = IllegalStateException.class)
    public void getProductDetailsRequiresInitialization() throws RemoteException {
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.getProductDetails(mock(Context.class), "", false, mock(ProductDetailsListener.class));
    }

    @Test
    public void getProductDetailsFailsIfDetailsNotFound() throws RemoteException {
        Bundle skuDetailsBundle = new Bundle();
        skuDetailsBundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
        skuDetailsBundle.putStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST, new ArrayList<String>());

        initialMockApi(true);
        when(api.getSkuDetails(anyString(), any(Bundle.class))).thenReturn(skuDetailsBundle);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        ProductDetailsListener listener = mock(ProductDetailsListener.class);
        vendor.getProductDetails(mock(Context.class), "", false, listener);

        verify(listener, times(1)).failure(new Vendor.Error(PRODUCT_DETAILS_NOT_FOUND, -1));
        verify(listener, never()).success(any(Product.class));
    }

    @Test
    public void getProductDetailsSuccessfulIfDetailsFound() throws RemoteException, JSONException {
        Bundle skuDetailsBundle = new Bundle();
        skuDetailsBundle.putInt(RESPONSE_CODE, BILLING_RESPONSE_RESULT_OK);
        skuDetailsBundle.putStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST, new ArrayList<String>() {{
            add(IN_APP_BILLING_PRODUCT_VALID_PRODUCT_JSON);
        }});

        initialMockApi(true);
        when(api.getSkuDetails(anyString(), any(Bundle.class))).thenReturn(skuDetailsBundle);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        ProductDetailsListener listener = mock(ProductDetailsListener.class);
        vendor.getProductDetails(mock(Context.class), "", false, listener);

        verify(listener, times(1)).success(InAppBillingProduct.create(IN_APP_BILLING_PRODUCT_VALID_PRODUCT_JSON, false));
        verify(listener, never()).failure(any(Vendor.Error.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProductFromRequiresValidVendorId() throws RemoteException, JSONException {
        final String invalidProductJson =
                "{\"micros-price\":1," +
                        "\"vendor-id\":\"" + VENDOR_PACKAGE + "_" + "\"," +
                        "\"price\":\"1\"," +
                        "\"name\":\"1\"," +
                        "\"description\":\"1\"," +
                        "\"currency\":\"1\"," +
                        "\"subscription\":false," +
                        "\"sku\":\"1\"}";

        initialMockApi(true);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        vendor.getProductFrom(new JSONObject(invalidProductJson));
    }

    @Test
    public void getProductFromReturnsProductForValidJson() throws RemoteException, JSONException {
        final String productJson =
                "{\"micros-price\":1," +
                        "\"vendor-id\":\"" + VENDOR_PACKAGE + "\"," +
                        "\"price\":\"1\"," +
                        "\"name\":\"1\"," +
                        "\"description\":\"1\"," +
                        "\"currency\":\"1\"," +
                        "\"subscription\":false," +
                        "\"sku\":\"1\"}";

        initialMockApi(true);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        Product productFrom = vendor.getProductFrom(new JSONObject(productJson));
        assertThat(productFrom).isEqualTo(Product.create(productJson));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPurchaseFromRequiresValidVendorId() throws RemoteException, JSONException {
        String invalidPurchaseJson = "{\"micros-price\":1," +
                "\"gp-package-name\":\"com.getkeepsafe.cashier.sample\"," +
                "\"vendor-id\":\"" + VENDOR_PACKAGE + "_\"," +
                "\"cashier-developer-payload\":\"hello-cashier!\"," +
                "\"cashier-order-id\":\"testtest\"," +
                "\"description\":\"1\"," +
                "\"gp-data-signature\":\"test\"," +
                "\"gp-purchase-data\":\"{\\\"orderId\\\":\\\"testtest\\\",\\\"autoRenewing\\\":false,\\\"packageName\\\":\\\"com.getkeepsafe.cashier.sample\\\",\\\"productId\\\":\\\"so.product.much.purchase\\\",\\\"purchaseTime\\\":1476077957823,\\\"purchaseState\\\":0,\\\"developerPayload\\\":\\\"hello-cashier!\\\",\\\"purchaseToken\\\":\\\"15d12f9b-82fc-4977-b49c-aef730a10463\\\"}\",\"subscription\":false,\"cashier-receipt\":\"{\\\"orderId\\\":\\\"testtest\\\",\\\"autoRenewing\\\":false,\\\"packageName\\\":\\\"com.getkeepsafe.cashier.sample\\\",\\\"productId\\\":\\\"so.product.much.purchase\\\",\\\"purchaseTime\\\":1476077957823,\\\"purchaseState\\\":0,\\\"developerPayload\\\":\\\"hello-cashier!\\\",\\\"purchaseToken\\\":\\\"15d12f9b-82fc-4977-b49c-aef730a10463\\\"}\",\"gp-purchase-state\":0,\"cashier-token\":\"15d12f9b-82fc-4977-b49c-aef730a10463\",\"price\":\"1\",\"gp-auto-renewing\":false,\"gp-purchase-time\":1476077957823,\"name\":\"1\",\"currency\":\"1\",\"sku\":\"so.product.much.purchase\"}\n";

        initialMockApi(true);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        vendor.getPurchaseFrom(new JSONObject(invalidPurchaseJson));
    }

    @Test
    public void getPurchaseFromReturnsProductForValidJson() throws RemoteException, JSONException {
        String purchaseJson = "{\"micros-price\":1," +
                "\"gp-package-name\":\"com.getkeepsafe.cashier.sample\"," +
                "\"vendor-id\":\"" + VENDOR_PACKAGE + "\"," +
                "\"cashier-developer-payload\":\"hello-cashier!\"," +
                "\"cashier-order-id\":\"testtest\"," +
                "\"description\":\"1\"," +
                "\"gp-data-signature\":\"test\"," +
                "\"gp-purchase-data\":\"{\\\"orderId\\\":\\\"testtest\\\",\\\"autoRenewing\\\":false,\\\"packageName\\\":\\\"com.getkeepsafe.cashier.sample\\\",\\\"productId\\\":\\\"so.product.much.purchase\\\",\\\"purchaseTime\\\":1476077957823,\\\"purchaseState\\\":0,\\\"developerPayload\\\":\\\"hello-cashier!\\\",\\\"purchaseToken\\\":\\\"15d12f9b-82fc-4977-b49c-aef730a10463\\\"}\",\"subscription\":false,\"cashier-receipt\":\"{\\\"orderId\\\":\\\"testtest\\\",\\\"autoRenewing\\\":false,\\\"packageName\\\":\\\"com.getkeepsafe.cashier.sample\\\",\\\"productId\\\":\\\"so.product.much.purchase\\\",\\\"purchaseTime\\\":1476077957823,\\\"purchaseState\\\":0,\\\"developerPayload\\\":\\\"hello-cashier!\\\",\\\"purchaseToken\\\":\\\"15d12f9b-82fc-4977-b49c-aef730a10463\\\"}\",\"gp-purchase-state\":0,\"cashier-token\":\"15d12f9b-82fc-4977-b49c-aef730a10463\",\"price\":\"1\",\"gp-auto-renewing\":false,\"gp-purchase-time\":1476077957823,\"name\":\"1\",\"currency\":\"1\",\"sku\":\"so.product.much.purchase\"}\n";

        initialMockApi(true);

        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);

        JSONObject jsonObject = new JSONObject(purchaseJson);
        Purchase purchase = vendor.getPurchaseFrom(jsonObject);
        assertThat(purchase).isEqualTo(InAppBillingPurchase.create(jsonObject));
    }


    private void mockDependeniesForSuccessfulPurchaseFlow() throws IntentSender.SendIntentException, RemoteException {
        final Bundle bundle = mock(Bundle.class);
        final PendingIntent pendingIntent = mock(PendingIntent.class);
        final IntentSender intentSender = mock(IntentSender.class);

        when(bundle.get(RESPONSE_CODE)).thenReturn(BILLING_RESPONSE_RESULT_OK);
        when(bundle.getParcelable(RESPONSE_BUY_INTENT)).thenReturn(pendingIntent);
        when(pendingIntent.getIntentSender()).thenReturn(intentSender);
        doNothing().when(activity).startIntentSenderForResult(any(IntentSender.class), anyInt(), any(Intent.class), anyInt(), anyInt(), anyInt());

        initialMockApi(true);
        when(api.getBuyIntent(anyString(), anyString(), or(isNull(String.class), anyString()))).thenReturn(bundle);
    }

    private void checkPurchaseAfterInitialization(boolean isInitializationSuccessfull) throws RemoteException {
        initialMockApi(isInitializationSuccessfull);
        InAppBillingV3Vendor vendor = new InAppBillingV3Vendor(api, null);
        vendor.initialize(mock(Context.class), initializationListener);
        Product inappPurchase = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "1",
                        "1",
                        "1",
                        "1",
                        "1",
                        false,
                        1);
        Product subscription = Product
                .create(
                        InAppBillingConstants.VENDOR_PACKAGE,
                        "1",
                        "1",
                        "1",
                        "1",
                        "1",
                        true,
                        1);
        assertThat(vendor.canPurchase(inappPurchase)).isEqualTo(isInitializationSuccessfull);
        assertThat(vendor.canPurchase(subscription)).isEqualTo(isInitializationSuccessfull);
    }

    private void initialMockApi(final boolean initSuccessfully) throws RemoteException {
        int billingSupportResult;
        if (initSuccessfully) {
            billingSupportResult = BILLING_RESPONSE_RESULT_OK;
        } else {
            billingSupportResult = BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE;
        }
        when(api.isBillingSupported(any(String.class))).thenReturn(billingSupportResult);
        when(api.initialize(any(Context.class), any(InAppBillingV3Vendor.class), any(AbstractInAppBillingV3API.LifecycleListener.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                ((AbstractInAppBillingV3API.LifecycleListener) invocationOnMock.getArgument(2)).initialized(initSuccessfully);
                return initSuccessfully;
            }
        });
        when(api.available()).thenReturn(initSuccessfully);
    }

}
