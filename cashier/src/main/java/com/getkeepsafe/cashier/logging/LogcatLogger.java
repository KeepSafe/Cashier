package com.getkeepsafe.cashier.logging;

import android.util.Log;

public class LogcatLogger implements Logger {
    private String tag;

    @Override
    public void setTag(String tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Null tag");
        }
        this.tag = tag;
    }

    @Override
    public void log(String message) {
        Log.d(tag, message);
    }
}
