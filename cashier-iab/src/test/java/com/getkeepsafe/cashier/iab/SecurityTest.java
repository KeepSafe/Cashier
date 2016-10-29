package com.getkeepsafe.cashier.iab;

import com.getkeepsafe.LibraryProjectRobolectricTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.PrivateKey;

import static com.getkeepsafe.cashier.iab.FakeInAppBillingV3Api.TEST_PRIVATE_KEY;
import static com.getkeepsafe.cashier.iab.FakeInAppBillingV3Api.TEST_PUBLIC_KEY;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LibraryProjectRobolectricTestRunner.class)
public class SecurityTest {
    final String purchaseData = "{\"autoRenewing\":false,\"orderId\":\"7429c5e9-f8e7-4332-b39d-60ce2c215fef\",\"packageName\":\"com.getkeepsafe.cashier.sample\",\"productId\":\"android.test.purchased\",\"purchaseTime\":1476077957823,\"purchaseState\":0,\"developerPayload\":\"hello-cashier!\",\"purchaseToken\":\"15d12f9b-82fc-4977-b49c-aef730a10463\"}";
    final String purchaseSignature =
            "kqxUG9i+Omsm73jYjNBVppC9wpjQxLecl6jF8so0PLhwDnTElHuCFLXGlmCwT1pL70M3ZTkgGRxR\n" +
            "vUqzn4utYbtWlfg4ASzLLahQbH3tZSQhD2KKvoy2BOTWTyi2XoqcftHS3qL+HgiSTEkxoxLyCyly\n" +
            "lNCSpPICv1DZEayAjLU=\n";
    @Test
    public void testPrivateKeyInValidFormat() {
        Security.createPrivateKey(TEST_PRIVATE_KEY);
    }

    @Test
    public void testPublicKeyInValidFormat() {
        Security.createPublicKey(TEST_PUBLIC_KEY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void signDataWithNoKeyOrDataThrows() {
        Security.sign("", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void signDataWithNoDataThrows() {
        Security.sign(Security.createPrivateKey(TEST_PRIVATE_KEY), "");
    }

    @Test
    public void signsData() {
        final PrivateKey privateKey = Security.createPrivateKey(TEST_PRIVATE_KEY);
        assertThat(Security.sign(privateKey, "test")).isEqualTo(
                "kUQ84k0Xr04JfpbNggZFmKHLgm2TLj3kCteV5N4OFCO2iFj6o+JSB/fufNjtAIiA8UglX3D1Bl9S\n" +
                "tDgmqaS1pAU5HKRFF+ZPldPZve6QghHfQ9mm1eGZfdDTD2U2TDDMB3FFb4lEQbnCDa6d25cE8qJi\n" +
                "LaclWepyd6tm4i500JM=\n");
    }

    @Test
    public void signsPurchaseData() {
        assertThat(Security.sign(TEST_PRIVATE_KEY, purchaseData)).isEqualTo(purchaseSignature);
    }

    @Test
    public void verifySignatureWithNoDataReturnsFalse() {
        assertThat(Security.verifySignature("", null, "")).isFalse();
    }

    @Test
    public void verifiesSignatures() {
        assertThat(Security.verifySignature(TEST_PUBLIC_KEY, purchaseData, purchaseSignature)).isTrue();
    }
}
