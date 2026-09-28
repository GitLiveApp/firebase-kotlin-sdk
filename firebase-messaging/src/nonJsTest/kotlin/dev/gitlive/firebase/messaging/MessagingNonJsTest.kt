/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.messaging

import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.isAutoInitEnabled
import com.google.firebase.messaging.messaging
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The parts of the `com.google.firebase.messaging` layer that need a Firebase app; the platform test classes initialise it. */
abstract class MessagingNonJsTest {

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
        assertTrue(messaging.isAutoInitEnabled)
    }
}
