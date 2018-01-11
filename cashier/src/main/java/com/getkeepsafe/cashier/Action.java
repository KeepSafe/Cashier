package com.getkeepsafe.cashier;

public interface Action<T> {
    void run(T obj);
}