package com.getkeepsafe.cashier;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

public class ShadowActivity extends Activity {

    @SuppressLint("StaticFieldLeak")
    static Cashier cashier;
    static Action<Activity> action;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (action != null && cashier != null) {
            action.run(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (cashier != null) {
            cashier.onActivityResult(requestCode, resultCode, data);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        action = null;
        cashier = null;
    }
}
