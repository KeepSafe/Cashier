package com.getkeepsafe.cashier;

import java.util.Random;

final class ValueFactory {
    static final char[] ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyz123457890".toCharArray();
    static final Random random = new Random();

    private ValueFactory() {}

    public static Product aProduct() {
        return new Product(
                aString(),
                aString(),
                aString(),
                aString(),
                aString(),
                aString(),
                aBoolean(),
                aLong());
    }

    public static Purchase aPurchase() {
        return new Purchase(aProduct(), aString(), aString(), aString());
    }

    public static long aLong() {
        return random.nextLong();
    }

    public static boolean aBoolean() {
        return random.nextBoolean();
    }

    public static String aString() {
        return aString(16);
    }

    public static String aString(final int length) {
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHANUMERIC[random.nextInt(length)]);
        }
        return builder.toString();
    }
}
