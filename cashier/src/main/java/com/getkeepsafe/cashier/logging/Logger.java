package com.getkeepsafe.cashier.logging;

public interface Logger {
    void setTag(String tag);
    void log(String message);
}
