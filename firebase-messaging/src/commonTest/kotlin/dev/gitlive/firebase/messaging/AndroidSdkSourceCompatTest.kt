/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.messaging

import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.SendException
import com.google.firebase.messaging.imageUrl
import com.google.firebase.messaging.link
import com.google.firebase.messaging.remoteMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Exercises the `com.google.firebase.messaging` layer as Android app code would, on every platform; the parts that
 * need a Firebase app are in the platform test classes.
 */
@IgnoreForAndroidUnitTest
class AndroidSdkSourceCompatTest {

    @Test
    fun testRemoteMessageBuilder() {
        val message: RemoteMessage = remoteMessage("846484016111@fcm.googleapis.com") {
            addData("first", "one")
            addData("second", "two")
            addData("first", null)
            setMessageId("compat-id")
            setMessageType("compat-type")
            setCollapseKey("compat-key")
            setTtl(60)
        }
        assertEquals(mapOf("second" to "two"), message.data)
        assertEquals("compat-id", message.messageId)
        assertEquals("compat-type", message.messageType)
        assertEquals("compat-key", message.collapseKey)
        assertEquals(60, message.ttl)
        @Suppress("DEPRECATION")
        assertEquals("846484016111@fcm.googleapis.com", message.to)
        assertEquals(RemoteMessage.PRIORITY_UNKNOWN, message.priority)
        assertEquals(RemoteMessage.PRIORITY_UNKNOWN, message.originalPriority)
        assertNull(message.notification)
        assertNull(message.from)
        assertEquals(0, message.sentTime)

        val replaced = RemoteMessage.Builder("846484016111@fcm.googleapis.com").setData(mapOf("a" to "b")).clearData().addData("c", "d").build()
        assertEquals(mapOf("c" to "d"), replaced.data)
    }

    @Test
    fun testConstants() {
        assertEquals(1, RemoteMessage.PRIORITY_HIGH)
        assertEquals(2, RemoteMessage.PRIORITY_NORMAL)
        assertEquals(0, SendException.ERROR_UNKNOWN)
        assertEquals(1, SendException.ERROR_INVALID_PARAMETERS)
        assertEquals(2, SendException.ERROR_SIZE)
        assertEquals(3, SendException.ERROR_TTL_EXCEEDED)
        assertEquals(4, SendException.ERROR_TOO_MANY_MESSAGES)
    }

    @Test
    fun testNotificationUrlsCompile() {
        val notification: RemoteMessage.Notification? = null
        assertNull(notification?.imageUrl)
        assertNull(notification?.link)
    }
}
