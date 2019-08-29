package com.getkeepsafe.cashier.billing;

import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.SkuDetails;
import com.getkeepsafe.cashier.Product;

public class GooglePlayBillingProduct {

    public static Product create(SkuDetails details, @SkuType String type) {
        return Product.create(
                GooglePlayBillingConstants.VENDOR_PACKAGE,
                details.getSku(),
                details.getPrice(),
                details.getPriceCurrencyCode(),
                details.getTitle(),
                details.getDescription(),
                type.equals(SkuType.SUBS),
                details.getPriceAmountMicros()
        );
    }
}
