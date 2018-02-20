package com.getkeepsafe.cashier.iab;

import com.getkeepsafe.cashier.Product;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.getkeepsafe.cashier.iab.InAppBillingTestData.IN_APP_BILLING_PRODUCT_VALID_PRODUCT_JSON;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.VALID_PRODUCT_JSON_CURRENCY;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.VALID_PRODUCT_JSON_DESCRIPTION;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.VALID_PRODUCT_JSON_MICROS_PRICE;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.VALID_PRODUCT_JSON_NAME;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.VALID_PRODUCT_JSON_PRICE;
import static com.getkeepsafe.cashier.iab.InAppBillingTestData.VALID_PRODUCT_JSON_SKU;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class InAppBillingProductTest {

    @Test(expected = JSONException.class)
    public void rejectsInvalidJson() throws JSONException {
        InAppBillingProduct.create("bad json", false);
    }

    @Test
    public void parsesJsonCorrectly() throws JSONException {
        Product product = InAppBillingProduct.create(IN_APP_BILLING_PRODUCT_VALID_PRODUCT_JSON, false);
        checkProduct(product);
    }

    @Test
    public void allFieldsAreAssigned() {
        Product product = InAppBillingProduct.create(
                VALID_PRODUCT_JSON_SKU,
                VALID_PRODUCT_JSON_PRICE,
                VALID_PRODUCT_JSON_CURRENCY,
                VALID_PRODUCT_JSON_NAME,
                VALID_PRODUCT_JSON_DESCRIPTION,
                false,
                VALID_PRODUCT_JSON_MICROS_PRICE
        );
        checkProduct(product);
    }

    private void checkProduct(Product product) {
        assertThat(product.currency()).isEqualTo(VALID_PRODUCT_JSON_CURRENCY);
        assertThat(product.description()).isEqualTo(VALID_PRODUCT_JSON_DESCRIPTION);
        assertThat(product.name()).isEqualTo(VALID_PRODUCT_JSON_NAME);
        assertThat(product.isSubscription()).isEqualTo(false);
        assertThat(product.sku()).isEqualTo(VALID_PRODUCT_JSON_SKU);
        assertThat(product.price()).isEqualTo(VALID_PRODUCT_JSON_PRICE);
        assertThat(product.vendorId()).isEqualTo(InAppBillingConstants.VENDOR_PACKAGE);
    }

}
