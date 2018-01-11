package com.getkeepsafe.cashier;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class ShadowFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    static Cashier cashier;
    static Action<Fragment> action;

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
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        action = null;
        cashier = null;
    }

}
