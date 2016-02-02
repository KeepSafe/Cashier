package com.getkeepsafe.cashier.utilities;

import android.support.annotation.Nullable;

public class Check {
    private Check() {
        throw new AssertionError("No Instances.");
    }

    public static <T> T notNull(@Nullable final T object) {
        return notNull(object, null);
    }

    public static <T> T notNull(@Nullable final T object, @Nullable final String objectName) {
        if (object == null) {
            if (objectName == null) {
                throw new NullPointerException("Required variable is null!");
            }

            throw new NullPointerException("Required object '" + objectName + "' is null!");
        }

        return object;
    }
}
