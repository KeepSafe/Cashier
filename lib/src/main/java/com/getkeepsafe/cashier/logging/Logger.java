package com.getkeepsafe.cashier.logging;

import android.support.annotation.NonNull;

public interface Logger {
    void setTag(@NonNull String tag);
    void log(@NonNull String message);
}
