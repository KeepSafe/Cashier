package com.getkeepsafe.cashier.googleplay;

public interface GooglePlayConstants {
    String VENDOR_PACKAGE = "com.android.vending";

    // API Response codes
    int BILLING_RESPONSE_RESULT_OK = 0;
    int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
    int BILLING_RESPONSE_RESULT_ERROR = 6;
    int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;

    // API Response data keys
    String RESPONSE_CODE = "RESPONSE_CODE";
    String RESPONSE_GET_SKU_DETAILS_LIST = "DETAILS_LIST";
    String RESPONSE_BUY_INTENT = "BUY_INTENT";
    String RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    String RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE";
    String RESPONSE_INAPP_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    String RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    String RESPONSE_INAPP_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

    // API Request commands
    String REQUEST_SKU_DETAILS_ITEM_LIST = "ITEM_ID_LIST";
    String REQUEST_SKU_DETAILS_ITEM_TYPE_LIST = "ITEM_TYPE_LIST";

    // Static test inapp products
    String TEST_PRODUCT_PURCHASED = "android.test.purchased";
    String TEST_PRODUCT_CANCELED = "android.test.canceled";
    String TEST_PRODUCT_REFUNDED = "android.test.refunded";
    String TEST_PRODUCT_UNAVAILABLE = "android.test.item_unavailable";

    // Product types
    String PRODUCT_TYPE_ITEM = "inapp";
    String PRODUCT_TYPE_SUBSCRIPTION = "subs";

    interface ProductConstants {
        String SKU = "productId";
        String PRICE = "price";
        String CURRENCY = "price_currency_code";
        String NAME = "title";
        String DESCRIPTION = "description";
        String PRICE_MICRO = "price_amount_micros";
    }
}
