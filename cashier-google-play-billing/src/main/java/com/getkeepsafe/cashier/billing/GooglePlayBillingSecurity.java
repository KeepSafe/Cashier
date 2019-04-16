/*
 *  Copyright 2019 Keepsafe Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.getkeepsafe.cashier.billing;

import android.text.TextUtils;
import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public final class GooglePlayBillingSecurity {
    private static final String KEY_TYPE = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    private static PublicKey createPublicKey(String publicKey64) {
        try {
            final byte[] decodedKey = Base64.decode(publicKey64, Base64.DEFAULT);
            final KeyFactory keyFactory = KeyFactory.getInstance(KEY_TYPE);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static PrivateKey createPrivateKey(String privateKey64) {
        try {
            final byte[] decodedKey = Base64.decode(privateKey64, Base64.DEFAULT);
            final KeyFactory keyFactory = KeyFactory.getInstance(KEY_TYPE);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sign(String privateKey64, String data) {
        if (TextUtils.isEmpty(privateKey64) || TextUtils.isEmpty(data)) {
            throw new IllegalArgumentException("Given null data to sign");
        }

        final PrivateKey privateKey = createPrivateKey(privateKey64);
        return sign(privateKey, data);
    }

    private static String sign(PrivateKey privateKey, String data) {
        if (privateKey == null || TextUtils.isEmpty(data)) {
            throw new IllegalArgumentException("Given null data to sign");
        }

        try {
            final Signature instance = Signature.getInstance(SIGNATURE_ALGORITHM);
            instance.initSign(privateKey);
            instance.update(data.getBytes());
            return Base64.encodeToString(instance.sign(), Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (SignatureException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    static boolean verifySignature(String publicKey64, String signedData, String signature64) {
        if (TextUtils.isEmpty(publicKey64) || TextUtils.isEmpty(signedData) || TextUtils.isEmpty(signature64)) {
            return false;
        }

        final PublicKey publicKey = createPublicKey(publicKey64);
        return verifySignature(publicKey, signedData, signature64);
    }

    private static boolean verifySignature(PublicKey publicKey, String signedData, String signature64) {
        try {
            final byte[] signature = Base64.decode(signature64, Base64.DEFAULT);
            final Signature instance = Signature.getInstance(SIGNATURE_ALGORITHM);
            instance.initVerify(publicKey);
            instance.update(signedData.getBytes());
            return instance.verify(signature);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (SignatureException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }
}
