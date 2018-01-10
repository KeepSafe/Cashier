package com.getkeepsafe.cashier;

import android.app.Activity;
import android.content.Intent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class ShadowActivityTest {

    private final Cashier cashier = mock(Cashier.class);
    private static final int REQUEST_CODE = 1;
    private static final int RESULT_CODE = Activity.RESULT_OK;

    @Test
    public void passesActivityResultToCashierAndFinishes() {
        ShadowActivity.action = new Action<Activity>() {
            @Override
            public void run(Activity activity) {
                activity.startActivityForResult(new Intent(), REQUEST_CODE);
            }
        };
        ShadowActivity.cashier = cashier;
        ActivityController controller = Robolectric.buildActivity(ShadowActivity.class).create();
        Activity activity = (Activity) controller.get();
        org.robolectric.shadows.ShadowActivity shadowActivity = shadowOf(activity);
        shadowActivity.receiveResult(new Intent(), Activity.RESULT_OK, null);
        verify(cashier, times(1)).onActivityResult(REQUEST_CODE, RESULT_CODE, null);
        assertTrue(shadowActivity.isFinishing());
    }

}
