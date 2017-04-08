/*
 *  Copyright 2017 Keepsafe Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.getkeepsafe.cashier.logging;

import android.util.Log;

public class LogcatLogger implements Logger {
  private static final String INTERNAL_TAG = "Cashier";

  @Override
  public void i(String tag, String message) {
    Log.i(INTERNAL_TAG + ":" + tag, message);
  }

  @Override
  public void w(String tag, String message) {
    Log.w(INTERNAL_TAG + ":" + tag, message);
  }

  @Override
  public void e(String tag, String message) {
    Log.e(INTERNAL_TAG + ":" + tag, message);
  }
}
