/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import kotlin.test.Test
import kotlin.test.assertEquals

/** The event, parameter and user property names have the Android SDK's values on every platform. */
class ConstantsTest {
    @Test
    fun eventNames() {
        assertEquals("purchase", FirebaseAnalytics.Event.PURCHASE)
        assertEquals("screen_view", FirebaseAnalytics.Event.SCREEN_VIEW)
        assertEquals("add_to_cart", FirebaseAnalytics.Event.ADD_TO_CART)
    }

    @Test
    fun paramNames() {
        assertEquals("currency", FirebaseAnalytics.Param.CURRENCY)
        assertEquals("item_list_id", FirebaseAnalytics.Param.ITEM_LIST_ID)
        assertEquals("screen_class", FirebaseAnalytics.Param.SCREEN_CLASS)
    }

    @Test
    fun userPropertyNames() {
        assertEquals("sign_up_method", FirebaseAnalytics.UserProperty.SIGN_UP_METHOD)
        assertEquals("allow_personalized_ads", FirebaseAnalytics.UserProperty.ALLOW_AD_PERSONALIZATION_SIGNALS)
    }
}
