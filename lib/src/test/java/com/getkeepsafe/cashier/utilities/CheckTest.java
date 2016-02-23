package com.getkeepsafe.cashier.utilities;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class CheckTest {
    @Test(expected = NullPointerException.class)
    public void throwsOnNullCheck() {
        Check.notNull(null);
    }

    @Test
    public void throwsNamedExceptionOnNullCheck() {
        final String badObjectName = "badObject";
        try {
            Check.notNull(null, badObjectName);
        } catch (NullPointerException e) {
            assertThat(e.getMessage(), containsString(badObjectName));
        }
    }

    @Test
    public void passesNonNullCheck() {
        final Object notNull = new Object();
        Check.notNull(notNull);
        Check.notNull(notNull, "notNull");
    }
}
