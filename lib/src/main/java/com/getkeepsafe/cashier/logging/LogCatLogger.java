package com.getkeepsafe.cashier.logging;

import android.support.annotation.NonNull;
import android.util.Log;

import com.getkeepsafe.cashier.utilities.Check;

public class LogCatLogger implements Logger {
    private String tag;

    @Override
    public void setTag(@NonNull String tag) {
        this.tag = Check.notNull(tag, "tag");
    }

    @Override
    public void log(@NonNull String message) {
        Log.d(tag, message);
    }
}
