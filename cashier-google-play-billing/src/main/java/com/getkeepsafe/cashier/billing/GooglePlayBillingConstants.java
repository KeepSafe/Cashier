/*
 *  Copyright 2019 Keepsafe Software, Inc.
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

package com.getkeepsafe.cashier.billing;

public final class GooglePlayBillingConstants {
    public static final String VENDOR_PACKAGE = "com.android.billingclient.api";

    private GooglePlayBillingConstants() {}

    public static class PurchaseConstants {
        public static final String PURCHASE_STATE = "purchaseState";
        public static final String DEVELOPER_PAYLOAD = "developerPayload";

        public static final int PURCHASE_STATE_PURCHASED = 0;
        public static final int PURCHASE_STATE_CANCELED = 1;
        public static final int PURCHASE_STATE_REFUNDED = 2;

        private PurchaseConstants() {}
    }
}
