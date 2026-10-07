/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.messaging

import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import dev.gitlive.firebase.UnsupportedOnJs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(UnsupportedOnJs::class)
abstract class FirebaseMessagingTest {

    /** False on JS, where the SDK never generates a token on its own and the setting is a no-op. */
    protected open val supportsAutoInit: Boolean = true

    @Test
    fun initialization() {
        assertNotNull(dev.gitlive.firebase.Firebase.messaging)
    }

    @Test
    fun testInstances() {
        val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()
        assertEquals(messaging, Firebase.messaging)
        assertEquals(messaging, dev.gitlive.firebase.Firebase.messaging.compat)
    }

    @Test
    fun testAutoInit() {
        val messaging = Firebase.messaging
        messaging.isAutoInitEnabled = false
        assertFalse(messaging.isAutoInitEnabled)
        messaging.isAutoInitEnabled = true
        assertEquals(supportsAutoInit, messaging.isAutoInitEnabled)
        assertTrue(messaging.isAutoInitEnabled || !supportsAutoInit)
    }
}
