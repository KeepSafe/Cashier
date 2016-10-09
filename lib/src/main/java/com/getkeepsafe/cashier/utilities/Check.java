package com.getkeepsafe.cashier.utilities;



public class Check {
    private Check() {
        throw new AssertionError("No Instances.");
    }

    public static <T> T notNull(final T object) {
        return notNull(object, null);
    }

    public static <T> T notNull(final T object, final String objectName) {
        if (object == null) {
            if (objectName == null) {
                throw new NullPointerException("Required variable is null!");
            }

            throw new NullPointerException("Required object '" + objectName + "' is null!");
        }

        return object;
    }
}
