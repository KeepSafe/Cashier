package com.getkeepsafe.cashier.iab;

public class InAppBillingConstants {
    public static final String VENDOR_PACKAGE = "com.android.vending";

    // API Response codes
    public static final int BILLING_RESPONSE_RESULT_OK = 0;
    public static final int BILLING_RESPONSE_RESULT_USER_CANCELED = 1;
    public static final int BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3;
    public static final int BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4;
    public static final int BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5;
    public static final int BILLING_RESPONSE_RESULT_ERROR = 6;
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    public static final int BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8;

    // API Response data keys
    public static final String RESPONSE_CODE = "RESPONSE_CODE";
    public static final String RESPONSE_GET_SKU_DETAILS_LIST = "DETAILS_LIST";
    public static final String RESPONSE_BUY_INTENT = "BUY_INTENT";
    public static final String RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
    public static final String RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE";
    public static final String RESPONSE_INAPP_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
    public static final String RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
    public static final String RESPONSE_INAPP_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
    public static final String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";

    // API Request commands
    public static final String REQUEST_SKU_DETAILS_ITEM_LIST = "ITEM_ID_LIST";
    public static final String REQUEST_SKU_DETAILS_ITEM_TYPE_LIST = "ITEM_TYPE_LIST";

    // Static test inapp products
    public static final String TEST_PRODUCT_PURCHASED = "android.test.purchased";
    public static final String TEST_PRODUCT_CANCELED = "android.test.canceled";
    public static final String TEST_PRODUCT_REFUNDED = "android.test.refunded";
    public static final String TEST_PRODUCT_UNAVAILABLE = "android.test.item_unavailable";

    // Product types
    public static final String PRODUCT_TYPE_ITEM = "inapp";
    public static final String PRODUCT_TYPE_SUBSCRIPTION = "subs";

    public static class ProductConstants {
        public static final String SKU = "productId";
        public static final String PRICE = "price";
        public static final String CURRENCY = "price_currency_code";
        public static final String NAME = "title";
        public static final String DESCRIPTION = "description";
        public static final String PRICE_MICRO = "price_amount_micros";
    }

    public static class PurchaseConstants {
        public static final String AUTO_RENEWING = "autoRenewing";
        public static final String ORDER_ID = "orderId";
        public static final String PACKAGE_NAME = "packageName";
        public static final String PRODUCT_ID = "productId";
        public static final String PURCHASE_TIME = "purchaseTime";
        public static final String PURCHASE_STATE = "purchaseState";
        public static final String DEVELOPER_PAYLOAD = "developerPayload";
        public static final String PURCHASE_TOKEN = "purchaseToken";

        public static final int PURCHASE_STATE_PURCHASED = 0;
        public static final int PURCHASE_STATE_CANCELED = 1;
        public static final int PURCHASE_STATE_REFUNDED = 2;
    }
}
