/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.messaging

import com.google.firebase.messaging.FirebaseMessaging
import dev.gitlive.firebase.UnsupportedOnJs
import dev.gitlive.firebase.messaging.externals.Messaging
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The members the JS SDK does not provide, on an instance over a bare object: `getMessaging()` needs a page with
 * service worker and push support, which the test browser does not give.
 */
@OptIn(UnsupportedOnJs::class)
class JsMessagingNoOpTest {

    private val messaging = FirebaseMessaging(js("{}").unsafeCast<Messaging>())

    @Test
    fun testTopicsAreNoOps() {
        val subscribed = messaging.subscribeToTopic("compat")
        assertTrue(subscribed.isSuccessful)
        assertTrue(messaging.unsubscribeFromTopic("compat").isSuccessful)
    }

    @Test
    fun testAutoInitIsOff() {
        assertFalse(messaging.isAutoInitEnabled)
        messaging.isAutoInitEnabled = true
        assertFalse(messaging.isAutoInitEnabled)
    }
}
