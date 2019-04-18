package com.getkeepsafe.cashier.billing;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Threading {

    private ExecutorService threadExecutor = Executors.newCachedThreadPool();

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    void runInBackground(Runnable runnable) {
        threadExecutor.execute(runnable);
    }

    void runOnMainThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

}
