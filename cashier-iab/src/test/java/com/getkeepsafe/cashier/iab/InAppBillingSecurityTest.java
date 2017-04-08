package com.getkeepsafe.cashier.iab;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.security.PrivateKey;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
public class InAppBillingSecurityTest {
  public static final String TEST_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALXolIcA1LIcYDnO\n" +
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

  public static final String TEST_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC16JSHANSyHGA5ztp32pW5Dg9l\n" +
      "AENyn6rHRBi3XRtq1vAspdiyMollb0Om6dquagaGAnbPZQUE9tbqNVR03L+Jp2i3\n" +
      "3QYzJc3nKyvMtRc0SyhV8qDKomrvMLpxXqhbHcx0O++D22MjYDBna81A16iHUDjS\n" +
      "/BKv9KYHXZNO7jccGwIDAQAB";

  final String purchaseData = "{\"autoRenewing\":false," +
      "\"orderId\":\"7429c5e9-f8e7-4332-b39d-60ce2c215fef\"," +
      "\"packageName\":\"com.getkeepsafe.cashier.sample\"," +
      "\"productId\":\"android.test.purchased\"," +
      "\"purchaseTime\":1476077957823," +
      "\"purchaseState\":0," +
      "\"developerPayload\":\"hello-cashier!\"," +
      "\"purchaseToken\":\"15d12f9b-82fc-4977-b49c-aef730a10463\"}";
  final String purchaseSignature =
      "kqxUG9i+Omsm73jYjNBVppC9wpjQxLecl6jF8so0PLhwDnTElHuCFLXGlmCwT1pL70M3ZTkgGRxR\n" +
          "vUqzn4utYbtWlfg4ASzLLahQbH3tZSQhD2KKvoy2BOTWTyi2XoqcftHS3qL+HgiSTEkxoxLyCyly\n" +
          "lNCSpPICv1DZEayAjLU=\n";

  @Test
  public void testPrivateKeyInValidFormat() {
    InAppBillingSecurity.createPrivateKey(TEST_PRIVATE_KEY);
  }

  @Test
  public void testPublicKeyInValidFormat() {
    InAppBillingSecurity.createPublicKey(TEST_PUBLIC_KEY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void signDataWithNoKeyOrDataThrows() {
    InAppBillingSecurity.sign("", "");
  }

  @Test(expected = IllegalArgumentException.class)
  public void signDataWithNoDataThrows() {
    InAppBillingSecurity.sign(InAppBillingSecurity.createPrivateKey(TEST_PRIVATE_KEY), "");
  }

  @Test
  public void signsData() {
    final PrivateKey privateKey = InAppBillingSecurity.createPrivateKey(TEST_PRIVATE_KEY);
    assertThat(InAppBillingSecurity.sign(privateKey, "test")).isEqualTo(
        "kUQ84k0Xr04JfpbNggZFmKHLgm2TLj3kCteV5N4OFCO2iFj6o+JSB/fufNjtAIiA8UglX3D1Bl9S\n" +
            "tDgmqaS1pAU5HKRFF+ZPldPZve6QghHfQ9mm1eGZfdDTD2U2TDDMB3FFb4lEQbnCDa6d25cE8qJi\n" +
            "LaclWepyd6tm4i500JM=\n");
  }

  @Test
  public void signsPurchaseData() {
    assertThat(InAppBillingSecurity.sign(TEST_PRIVATE_KEY, purchaseData)).isEqualTo(purchaseSignature);
  }

  @Test
  public void verifySignatureWithNoDataReturnsFalse() {
    assertThat(InAppBillingSecurity.verifySignature("", null, "")).isFalse();
  }

  @Test
  public void verifiesSignatures() {
    assertThat(InAppBillingSecurity.verifySignature(TEST_PUBLIC_KEY, purchaseData, purchaseSignature)).isTrue();
  }
}
