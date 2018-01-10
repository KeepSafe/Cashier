package com.getkeepsafe.cashier.iab;

public interface InAppBillingTestData {
    String TEST_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALXolIcA1LIcYDnO\n" +
            "2nfalbkOD2UAQ3KfqsdEGLddG2rW8Cyl2LIyiWVvQ6bp2q5qBoYCds9lBQT21uo1\n" +
            "VHTcv4mnaLfdBjMlzecrK8y1FzRLKFXyoMqiau8wunFeqFsdzHQ774PbYyNgMGdr\n" +
            "zUDXqIdQONL8Eq/0pgddk07uNxwbAgMBAAECgYAJInvK57zGkOw4Gu4XlK9uEomt\n" +
            "Xb0FVYVC6mV/V7qXu+FlrJJcKHOD13mDOT0VAxf+xMLomT8OR8L1EeaC087+aeza\n" +
            "twYUVx4d+J0cQ8xo3ILwY5Bg4/Y4R0gIbdKupHbhPKaLSAiMxilNKqNfY8upT2X/\n" +
            "S4OFDDbm7aK8SlGPEQJBAN+YlMb4PS54aBpWgeAP8fzgtOL0Q157bmoQyCokiWv3\n" +
            "OGa89LraifCtlsqmmAxyFbPzO2cFHYvzzEeU86XZVFkCQQDQRWQ0QJKJsfqxEeYG\n" +
            "rq9e3TkY8uQeHz8BmgxRcYC0v43bl9ggAJAzh9h9o0X9da1YzkoQ0/cWUp5NK95F\n" +
            "93WTAkEAxqm1/rcO/RwEOuqDyIXCVxF8Bm5K8UawCtNQVYlTBDeKyFW5B9AmYU6K\n" +
            "vRGZ5Oz0dYd2TwlPgEqkRTGF7eSUOQJAfyK85oC8cz2oMMsiRdYAy8Hzht1Oj2y3\n" +
            "g3zMJDNLRArix7fLgM2XOT2l1BwFL5HUPa+/2sHpxUCtzaIHz2Id7QJATyF+fzUR\n" +
            "eVw04ogIsOIdG0ECrN5/3g9pQnAjxcReQ/4KVCpIE8lQFYjAzQYUkK9VOjX9LYp9\n" +
            "DGEnpooCco1ZjA==";

    String TEST_PUBLIC_KEY_1 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC16JSHANSyHGA5ztp32pW5Dg9l\n" +
            "AENyn6rHRBi3XRtq1vAspdiyMollb0Om6dquagaGAnbPZQUE9tbqNVR03L+Jp2i3\n" +
            "3QYzJc3nKyvMtRc0SyhV8qDKomrvMLpxXqhbHcx0O++D22MjYDBna81A16iHUDjS\n" +
            "/BKv9KYHXZNO7jccGwIDAQAB";

    String TEST_PUBLIC_KEY_2 = "NIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC16JSHANSyHGA5ztp32pW5Dg9l\n" +
            "AENyn6rHRBi3XRtq1vAspdiyMollb0Om6dquagaGAnbPZQUE9tbqNVR03L+Jp2i3\n" +
            "3QYzJc3nKyvMtRc0SyhV8qDKomrvMLpxXqhbHcx0O++D22MjYDBna81A16iHUDjS\n" +
            "/BKv9KYHXZNO7jccGwIDAQAB";

    String IN_APP_BILLING_PURCHASE_VALID_PRODUCT_JSON = "{\"micros-price\":1," +
            "\"vendor-id\":\"1\"," +
            "\"price\":\"1\"," +
            "\"name\":\"1\"," +
            "\"description\":\"1\"," +
            "\"currency\":\"1\"," +
            "\"subscription\":false," +
            "\"sku\":\"so.product.much.purchase\"}";

    String VALID_TEST_PURCHASE_RECEIPT_JSON = "{\"autoRenewing\":false," +
            "\"packageName\":\"com.getkeepsafe.cashier.sample\"," +
            "\"productId\":\"so.product.much.purchase\"," +
            "\"purchaseTime\":1476077957823," +
            "\"purchaseState\":0," +
            "\"developerPayload\":\"hello-cashier!\"," +
            "\"purchaseToken\":\"15d12f9b-82fc-4977-b49c-aef730a10463\"}";

    String VALID_PURCHASE_RECEIPT_JSON = "{\"orderId\":\"testtest\"," +
            "\"autoRenewing\":false," +
            "\"packageName\":\"com.getkeepsafe.cashier.sample\"," +
            "\"productId\":\"so.product.much.purchase\"," +
            "\"purchaseTime\":1476077957823," +
            "\"purchaseState\":0," +
            "\"developerPayload\":\"hello-cashier!\"," +
            "\"purchaseToken\":\"15d12f9b-82fc-4977-b49c-aef730a10463\"}";

    String VALID_ONE_TIME_PURCHASE_JSON = "{\"micros-price\":1," +
            "\"gp-package-name\":\"com.getkeepsafe.cashier.sample\"," +
            "\"vendor-id\":\"1\"," +
            "\"cashier-developer-payload\":\"hello-cashier!\"," +
            "\"cashier-order-id\":\"testtest\"," +
            "\"description\":\"1\"," +
            "\"gp-data-signature\":\"test\"," +
            "\"gp-purchase-data\":\"{\\\"orderId\\\":\\\"testtest\\\",\\\"autoRenewing\\\":false,\\\"packageName\\\":\\\"com.getkeepsafe.cashier.sample\\\",\\\"productId\\\":\\\"so.product.much.purchase\\\",\\\"purchaseTime\\\":1476077957823,\\\"purchaseState\\\":0,\\\"developerPayload\\\":\\\"hello-cashier!\\\",\\\"purchaseToken\\\":\\\"15d12f9b-82fc-4977-b49c-aef730a10463\\\"}\",\"subscription\":false,\"cashier-receipt\":\"{\\\"orderId\\\":\\\"testtest\\\",\\\"autoRenewing\\\":false,\\\"packageName\\\":\\\"com.getkeepsafe.cashier.sample\\\",\\\"productId\\\":\\\"so.product.much.purchase\\\",\\\"purchaseTime\\\":1476077957823,\\\"purchaseState\\\":0,\\\"developerPayload\\\":\\\"hello-cashier!\\\",\\\"purchaseToken\\\":\\\"15d12f9b-82fc-4977-b49c-aef730a10463\\\"}\",\"gp-purchase-state\":0,\"cashier-token\":\"15d12f9b-82fc-4977-b49c-aef730a10463\",\"price\":\"1\",\"gp-auto-renewing\":false,\"gp-purchase-time\":1476077957823,\"name\":\"1\",\"currency\":\"1\",\"sku\":\"so.product.much.purchase\"}\n";

    String VALID_SUBSCRIPTION_PURCHASE_JSON = "{\"micros-price\":1," +
            "\"gp-package-name\":\"com.getkeepsafe.cashier.sample\"," +
            "\"vendor-id\":\"1\"," +
            "\"cashier-developer-payload\":\"hello-cashier!\"," +
            "\"cashier-order-id\":\"testtest\"," +
            "\"description\":\"1\"," +
            "\"gp-data-signature\":\"test\"," +
            "\"gp-purchase-data\":\"{\\\"orderId\\\":\\\"testtest\\\",\\\"autoRenewing\\\":false,\\\"packageName\\\":\\\"com.getkeepsafe.cashier.sample\\\",\\\"productId\\\":\\\"so.product.much.purchase\\\",\\\"purchaseTime\\\":1476077957823,\\\"purchaseState\\\":0,\\\"developerPayload\\\":\\\"hello-cashier!\\\",\\\"purchaseToken\\\":\\\"15d12f9b-82fc-4977-b49c-aef730a10463\\\"}\",\"subscription\":true,\"cashier-receipt\":\"{\\\"orderId\\\":\\\"testtest\\\",\\\"autoRenewing\\\":false,\\\"packageName\\\":\\\"com.getkeepsafe.cashier.sample\\\",\\\"productId\\\":\\\"so.product.much.purchase\\\",\\\"purchaseTime\\\":1476077957823,\\\"purchaseState\\\":0,\\\"developerPayload\\\":\\\"hello-cashier!\\\",\\\"purchaseToken\\\":\\\"15d12f9b-82fc-4977-b49c-aef730a10463\\\"}\",\"gp-purchase-state\":0,\"cashier-token\":\"15d12f9b-82fc-4977-b49c-aef730a10463\",\"price\":\"1\",\"gp-auto-renewing\":false,\"gp-purchase-time\":1476077957823,\"name\":\"1\",\"currency\":\"1\",\"sku\":\"so.product.much.purchase\"}\n";

    long VALID_PRODUCT_JSON_MICROS_PRICE = 1000;

    String VALID_PRODUCT_JSON_PRICE = "1";

    String VALID_PRODUCT_JSON_NAME = "1";

    String VALID_PRODUCT_JSON_DESCRIPTION = "1";

    String VALID_PRODUCT_JSON_CURRENCY = "1";

    boolean VALID_PRODUCT_JSON_IS_SUBSCRIPTION = false;

    String VALID_PRODUCT_JSON_SKU = "so.product.much.purchase";

    String IN_APP_BILLING_PRODUCT_VALID_PRODUCT_JSON = "{\"price_amount_micros\":" + VALID_PRODUCT_JSON_MICROS_PRICE +
            ", \"price\":\"" + VALID_PRODUCT_JSON_PRICE + "\"" +
            ", \"title\":\"" + VALID_PRODUCT_JSON_NAME + "\"" +
            ", \"description\":\"" + VALID_PRODUCT_JSON_DESCRIPTION + "\"" +
            ", \"price_currency_code\":\"" + VALID_PRODUCT_JSON_CURRENCY + "\"" +
            ", \"subscription\":" + VALID_PRODUCT_JSON_IS_SUBSCRIPTION + "\"" +
            ", \"productId\":\"" + VALID_PRODUCT_JSON_SKU + "\"}";
}
