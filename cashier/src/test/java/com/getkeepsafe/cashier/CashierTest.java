package com.getkeepsafe.cashier;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CashierTest {
  final String TEST_VENDOR_ID = "test";
  final Context context = mock(Context.class);
  final PackageManager packageManager = mock(PackageManager.class);
  final Vendor testVendor = mock(Vendor.class);

  final VendorFactory testVendorFactory = new VendorFactory() {
    @Override
    public Vendor create() {
      return testVendor;
    }
  };

  final Answer<Void> initializationSuccess = new Answer<Void>() {
    @Override
    public Void answer(InvocationOnMock invocation) throws Throwable {
      ((Vendor.InitializationListener) invocation.getArgument(1)).initialized();
      return null;
    }
  };

  final Answer<Void> initializationFailure = new Answer<Void>() {
    @Override
    public Void answer(InvocationOnMock invocation) throws Throwable {
      ((Vendor.InitializationListener) invocation.getArgument(1)).unavailable();
      return null;
    }
  };

  final Answer<Boolean> ifProductIsFromTestVendor = new Answer<Boolean>() {
    @Override
    public Boolean answer(InvocationOnMock invocation) throws Throwable {
      return ((Product) invocation.getArgument(0)).vendorId().equals(TEST_VENDOR_ID);
    }
  };

  @Before
  public void setup() {
    Cashier.putVendorFactory(TEST_VENDOR_ID, testVendorFactory);
    when(testVendor.id()).thenReturn(TEST_VENDOR_ID);
    when(context.getPackageManager()).thenReturn(packageManager);
    when(context.getPackageName()).thenReturn(TEST_VENDOR_ID);
    when(packageManager.getInstallerPackageName(TEST_VENDOR_ID)).thenReturn(TEST_VENDOR_ID);
  }

  @Test
  public void getsVendorFactory() {
    assertThat(Cashier.getVendorFactory(TEST_VENDOR_ID)).isEqualTo(testVendorFactory);
  }

  @Test
  public void getsInstanceFromInstaller() {
    assertThat(Cashier.forInstaller(context).build().vendorId()).isEqualTo(TEST_VENDOR_ID);
  }

  @Test
  public void getsInstanceFromVendor() {
    assertThat(Cashier.forVendor(context, testVendor).build().vendorId()).isEqualTo(TEST_VENDOR_ID);
  }

  @Test
  public void getsInstanceFromProduct() {
    final Product product = Product.create(TEST_VENDOR_ID, "a", "a", "a", "a", "a", true, 1L);
    assertThat(Cashier.forProduct(context, product).build().vendorId()).isEqualTo(TEST_VENDOR_ID);
  }

  @Test
  public void getsInstanceFromPurchase() {
    final Product product = Product.create(TEST_VENDOR_ID, "a", "a", "a", "a", "a", true, 1L);
    final Purchase purchase = CashierPurchase.create(product, "a", "a", "a", "a");
    assertThat(Cashier.forPurchase(context, purchase).build().vendorId()).isEqualTo(TEST_VENDOR_ID);
  }

  @Test
  public void getsProductFromVendor() throws JSONException {
    when(testVendor.getProductFrom(any(JSONObject.class))).thenAnswer(new Answer<Product>() {
      @Override
      public Product answer(InvocationOnMock invocation) throws Throwable {
        return Product.create((JSONObject) invocation.getArgument(0));
      }
    });

    final String productJson =
        "{\"micros-price\":1," +
            "\"vendor-id\":\"" + TEST_VENDOR_ID + "\"," +
            "\"price\":\"1\"," +
            "\"name\":\"1\"," +
            "\"description\":\"1\"," +
            "\"currency\":\"1\"," +
            "\"subscription\":false," +
            "\"sku\":\"1\"}";
    final Product fromVendor = Cashier.productFromVendor(productJson);
    assertThat(fromVendor).isEqualTo(Product.create(productJson));
  }

  @Test
  public void getsPurchaseFromVendor() throws JSONException {
    when(testVendor.getPurchaseFrom(any(JSONObject.class))).thenAnswer(new Answer<CashierPurchase>() {
      @Override
      public CashierPurchase answer(InvocationOnMock invocation) throws Throwable {
        return CashierPurchase.create((JSONObject) invocation.getArgument(0));
      }
    });

    final String purchaseJson =
        "{\"micros-price\":1," +
            "\"cashier-token\":\"a\"," +
            "\"vendor-id\":\"" + TEST_VENDOR_ID + "\"," +
            "\"cashier-developer-payload\":\"a\"," +
            "\"price\":\"1\"," +
            "\"name\":\"a\"," +
            "\"cashier-order-id\":\"a\"," +
            "\"description\":\"a\"," +
            "\"currency\":\"a\"," +
            "\"subscription\":true," +
            "\"sku\":\"a\"," +
            "\"cashier-receipt\":\"a\"}";
    final Purchase fromVendor = Cashier.purchaseFromVendor(purchaseJson);
    assertThat(fromVendor).isEqualTo(CashierPurchase.create(purchaseJson));
  }

  @Test
  public void purchaseUnavailableVendor() {
    doAnswer(initializationSuccess).when(testVendor).initialize(any(Context.class), any(Vendor.InitializationListener.class));
    when(testVendor.available()).thenReturn(false);

    final AppCompatActivity activity = mock(AppCompatActivity.class);
    final Cashier cashier = Cashier.forVendor(context, testVendor).build();
    final Product product = ValueFactory.aProduct();
    final PurchaseListener listener = mock(PurchaseListener.class);

    cashier.purchase(activity, product, listener);

    verify(listener).failure(product, new Vendor.Error(VendorConstants.PURCHASE_UNAVAILABLE, -1));
    verify(testVendor, never()).purchase(any(ShadowFragment.class), any(Product.class), or(isNull(String.class), anyString()), any(PurchaseListener.class));
  }

  @Test
  public void purchaseProductWrongVendor() {
    doAnswer(initializationSuccess).when(testVendor).initialize(any(Context.class), any(Vendor.InitializationListener.class));
    doAnswer(ifProductIsFromTestVendor).when(testVendor).canPurchase(any(Product.class));
    when(testVendor.available()).thenReturn(true);

    final AppCompatActivity activity = mock(AppCompatActivity.class);
    final Cashier cashier = Cashier.forVendor(context, testVendor).build();
    final Product product = ValueFactory.aProduct();
    final PurchaseListener listener = mock(PurchaseListener.class);

    cashier.purchase(activity, product, listener);

    verify(listener).failure(product, new Vendor.Error(VendorConstants.PURCHASE_UNAVAILABLE, -1));
    verify(testVendor, never()).purchase(any(ShadowFragment.class), any(Product.class), or(isNull(String.class), anyString()), any(PurchaseListener.class));
  }

  @Test
  public void purchaseUninitializedVendor() {
    doAnswer(initializationFailure).when(testVendor).initialize(any(Context.class), any(Vendor.InitializationListener.class));

    final AppCompatActivity activity = mock(AppCompatActivity.class);
    final Cashier cashier = Cashier.forVendor(context, testVendor).build();
    final Product product = ValueFactory.aProduct();
    final PurchaseListener listener = mock(PurchaseListener.class);

    cashier.purchase(activity, product, listener);

    verify(listener).failure(product, new Vendor.Error(VendorConstants.PURCHASE_UNAVAILABLE, -1));
    verify(testVendor, never()).purchase(any(ShadowFragment.class), any(Product.class), or(isNull(String.class), anyString()), any(PurchaseListener.class));
  }

  @Test
  public void purchase() throws JSONException {
    doAnswer(initializationSuccess).when(testVendor).initialize(any(Context.class), any(Vendor.InitializationListener.class));
    doAnswer(ifProductIsFromTestVendor).when(testVendor).canPurchase(any(Product.class));
    when(testVendor.available()).thenReturn(true);

    FragmentManager fragmentManager = mock(FragmentManager.class);
    FragmentTransaction fragmentTransaction = mock(FragmentTransaction.class);
    when(fragmentManager.beginTransaction()).thenReturn(fragmentTransaction);
    when(fragmentTransaction.add(any(Fragment.class), or(isNull(String.class), anyString()))).thenReturn(fragmentTransaction);
    when(fragmentTransaction.commit()).thenReturn(0);

    final AppCompatActivity activity = mock(AppCompatActivity.class);
    when(activity.getSupportFragmentManager()).thenReturn(fragmentManager);
    final Cashier cashier = Cashier.forVendor(context, testVendor).build();
    final Product product = Product.create(
        "{\"micros-price\":1," +
            "\"vendor-id\":\"" + TEST_VENDOR_ID + "\"," +
            "\"price\":\"1\"," +
            "\"name\":\"1\"," +
            "\"description\":\"1\"," +
            "\"currency\":\"1\"," +
            "\"subscription\":false," +
            "\"sku\":\"1\"}");
    final PurchaseListener listener = mock(PurchaseListener.class);
    final String devPayload = "abc";

    cashier.purchase(activity, product, devPayload, listener);
    verify(fragmentManager, times(1)).beginTransaction();
    verifyZeroInteractions(listener);
  }
}
