package com.getkeepsafe.cashier.billing;

import com.android.billingclient.api.SkuDetails;
import com.getkeepsafe.cashier.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestData {

    static final String TEST_PRIVATE_KEY =
            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALXolIcA1LIcYDnO\n" +
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

    static final String TEST_PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC16JSHANSyHGA5ztp32pW5Dg9l\n" +
                    "AENyn6rHRBi3XRtq1vAspdiyMollb0Om6dquagaGAnbPZQUE9tbqNVR03L+Jp2i3\n" +
                    "3QYzJc3nKyvMtRc0SyhV8qDKomrvMLpxXqhbHcx0O++D22MjYDBna81A16iHUDjS\n" +
                    "/BKv9KYHXZNO7jccGwIDAQAB";

    public static Product productInappA = Product.create(
            GooglePlayBillingConstants.VENDOR_PACKAGE,
            "com.test.product.inapp.A",
            "$0.99",
            "USD",
            "Test product A",
            "This is a test product",
            false,
            990_000L);

    public static Product productInappB = Product.create(
            GooglePlayBillingConstants.VENDOR_PACKAGE,
            "com.test.product.inapp.N",
            "$123.99",
            "USD",
            "Test product B",
            "This is another test product",
            false,
            123990_000L);

    public static Product productSubA = Product.create(
            GooglePlayBillingConstants.VENDOR_PACKAGE,
            "com.test.product.sub.A",
            "$123.99",
            "USD",
            "Test subscription A",
            "This is test subscription",
            true,
            123990_000L);

    public static List<Product> allInAppProducts = new ArrayList<>();

    public static List<Product> allSubProducts = new ArrayList<>();

    public static List<Product> allProducts = new ArrayList<>();

    public static List<String> allInAppSkus = new ArrayList<>();

    public static List<String> allSubSkus = new ArrayList<>();

    public static List<String> allSkus = new ArrayList<>();

    public static Map<String, Product> productsMap = new HashMap<>();

    static {
        allInAppProducts.add(productInappA);
        allInAppProducts.add(productInappB);

        allSubProducts.add(productSubA);

        allInAppSkus.add(productInappA.sku());
        allInAppSkus.add(productInappB.sku());

        allSubSkus.add(productSubA.sku());

        allProducts.addAll(allInAppProducts);
        allProducts.addAll(allSubProducts);

        allSkus.addAll(allInAppSkus);
        allSkus.addAll(allSubSkus);

        for (Product product : allProducts) {
            productsMap.put(product.sku(), product);
        }
    }

    static SkuDetails getSkuDetail(String sku) {
        try {
            return new TestSkuDetails(productsMap.get(sku));
        } catch (Exception e) {
            return null;
        }
    }

    static Map<String, SkuDetails> getSkuDetailsMap(List<String> skus) {
        Map<String, SkuDetails> map = new HashMap<>();
        for (String sku : skus) {
            try {
                map.put(sku, new TestSkuDetails(productsMap.get(sku)));
            } catch (Exception e) {}
        }
        return map;
    }
}
