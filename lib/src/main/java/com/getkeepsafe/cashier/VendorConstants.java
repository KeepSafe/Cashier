package com.getkeepsafe.cashier;

public interface VendorConstants {
    // Purchases
    /** Cannot buy the product, either do to no network connectivity or vendor being unavailable */
    int PURCHASE_UNAVAILABLE = 0;

    /** User canceled the purchase */
    int PURCHASE_CANCELED = 1;

    /** Unknown error **/
    int PURCHASE_FAILURE = 2;

    /** User already owns the product */
    int PURCHASE_ALREADY_OWNED = 3;

    /** User does not own the product */
    int PURCHASE_NOT_OWNED = 4;

    /** Purchase seems to be successful, however the expected result is malformed */
    int PURCHASE_SUCCESS_RESULT_MALFORMED = 5;

    // Consuming
    /** Unknown consume error */
    int CONSUME_FAILURE = 0;

    /**
     * Cannot consume the purchase, either due to no network connectivity or vendor being
     * unavailable
     * */
    int CONSUME_UNAVAILABLE = 1;

    /** User canceled the consume */
    int CONSUME_CANCELED = 2;

    /** User doesn't own the purchase to consume */
    int CONSUME_NOT_OWNED = 3;

    // Inventory
    /** Unknown inventory querying error */
    int INVENTORY_QUERY_FAILURE = 0;

    /** Inventory query returned a malformed response */
    int INVENTORY_QUERY_MALFORMED_RESPONSE = 1;
}
