package com.getkeepsafe.cashier.billing;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.getkeepsafe.cashier.Inventory;
import com.getkeepsafe.cashier.InventoryListener;
import com.getkeepsafe.cashier.Product;
import com.getkeepsafe.cashier.Vendor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import edu.emory.mathcs.backport.java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class InventoryQueryTest {

    @Mock
    AbstractGooglePlayBillingApi api;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(api.getPurchases()).thenReturn(new ArrayList<Purchase>());
        when(api.getPurchases(anyString())).thenReturn(new ArrayList<Purchase>());

        TestHelper.mockSkuDetails(api, BillingClient.SkuType.INAPP, TestData.getSkuDetailsMap(TestData.allInAppSkus));
        TestHelper.mockSkuDetails(api, BillingClient.SkuType.SUBS, TestData.getSkuDetailsMap(TestData.allSubSkus));
        TestHelper.mockPurchases(api, Collections.singletonList(TestData.productInappA));
    }

    @Test
    public void returns_inventory() {
        InventoryListener listener = mock(InventoryListener.class);
        InventoryQuery.execute(
                TestHelper.mockThreading(),
                api,
                listener,
                TestData.allInAppSkus,
                TestData.allSubSkus
        );
        ArgumentCaptor<Inventory> argument = ArgumentCaptor.forClass(Inventory.class);

        // Check if API gets called correctly
        verify(api).getPurchases(BillingClient.SkuType.SUBS);
        verify(api).getPurchases(BillingClient.SkuType.INAPP);
        verify(api).getSkuDetails(eq(BillingClient.SkuType.SUBS), eq(TestData.allSubSkus), any(SkuDetailsResponseListener.class));
        verify(api).getSkuDetails(eq(BillingClient.SkuType.INAPP), eq(TestData.allInAppSkus), any(SkuDetailsResponseListener.class));

        // Check if result is delivered
        verify(listener).success(argument.capture());
        assertEquals(TestData.allProducts.size(), argument.getValue().products().size());
        assertEquals(1, argument.getValue().purchases().size());
        for (Product product : argument.getValue().products()) {
            assertTrue(TestData.allProducts.contains(product));
        }
    }

    @Test
    public void returns_error_when_inapp_purchases_call_fails() {
        when(api.getPurchases(BillingClient.SkuType.INAPP)).thenReturn(null);
        when(api.getPurchases(BillingClient.SkuType.SUBS)).thenReturn(new ArrayList<Purchase>());

        InventoryListener listener = mock(InventoryListener.class);
        InventoryQuery.execute(
                TestHelper.mockThreading(),
                api,
                listener,
                TestData.allInAppSkus,
                TestData.allSubSkus
        );

        verify(listener).failure(any(Vendor.Error.class));
    }

    @Test
    public void returns_error_when_sub_purchases_call_fails() {
        when(api.getPurchases(BillingClient.SkuType.SUBS)).thenReturn(null);
        when(api.getPurchases(BillingClient.SkuType.INAPP)).thenReturn(new ArrayList<Purchase>());

        InventoryListener listener = mock(InventoryListener.class);
        InventoryQuery.execute(
                TestHelper.mockThreading(),
                api,
                listener,
                TestData.allInAppSkus,
                TestData.allSubSkus
        );

        verify(listener).failure(any(Vendor.Error.class));
    }

    @Test
    public void returns_error_when_inapp_sku_details_call_fails() {
        TestHelper.mockSkuDetailsError(api, BillingClient.SkuType.INAPP);

        InventoryListener listener = mock(InventoryListener.class);
        InventoryQuery.execute(
                TestHelper.mockThreading(),
                api,
                listener,
                TestData.allInAppSkus,
                TestData.allSubSkus
        );

        verify(listener).failure(any(Vendor.Error.class));
    }

    @Test
    public void returns_error_when_subs_sku_details_call_fails() {
        TestHelper.mockSkuDetailsError(api, BillingClient.SkuType.SUBS);

        InventoryListener listener = mock(InventoryListener.class);
        InventoryQuery.execute(
                TestHelper.mockThreading(),
                api,
                listener,
                TestData.allInAppSkus,
                TestData.allSubSkus
        );

        verify(listener).failure(any(Vendor.Error.class));
    }

    @Test
    public void returns_inventory_when_subs_feature_not_available() {
        when(api.isBillingSupported(BillingClient.SkuType.SUBS)).thenReturn(BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED);

        InventoryListener listener = mock(InventoryListener.class);
        InventoryQuery.execute(
                TestHelper.mockThreading(),
                api,
                listener,
                TestData.allInAppSkus,
                TestData.allSubSkus
        );
        ArgumentCaptor<Inventory> argument = ArgumentCaptor.forClass(Inventory.class);

        // Check if API gets called correctly
        verify(api).getPurchases(BillingClient.SkuType.INAPP);
        verify(api, never()).getPurchases(BillingClient.SkuType.SUBS);
        verify(api).getSkuDetails(eq(BillingClient.SkuType.INAPP), eq(TestData.allInAppSkus), any(SkuDetailsResponseListener.class));
        verify(api, Mockito.never()).getSkuDetails(eq(BillingClient.SkuType.SUBS), eq(TestData.allSubSkus), any(SkuDetailsResponseListener.class));

        // Check if result is delivered
        verify(listener).success(argument.capture());
        assertEquals(TestData.allInAppProducts.size(), argument.getValue().products().size());
        assertEquals(1, argument.getValue().purchases().size());
        for (Product product : argument.getValue().products()) {
            assertTrue(TestData.allProducts.contains(product));
        }
    }
}
