/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import com.google.android.gms.tasks.Task

/**
 * Firebase Cloud Messaging, as the Android SDK's `com.google.firebase.messaging.FirebaseMessaging`. Topic
 * subscriptions and auto-init exist on Android, the JVM and Apple platforms, as extensions in `nonJsMain`; the JS SDK
 * has neither. Receiving messages is platform code on every platform (`FirebaseMessagingService` on Android).
 */
public expect class FirebaseMessaging {
    /** Deletes the registration token of this app instance, so it stops receiving messages. */
    public fun deleteToken(): Task<Nothing?>

    /** The registration token of this app instance, requested from the backend if needed. */
    public fun getToken(): Task<String>

    public companion object {
        /** The scope of the registration token, `FCM`; a remnant of the Instance ID API. */
        @Deprecated("The registration token has no scope any more; getToken() returns the FCM token")
        public val INSTANCE_ID_SCOPE: String

        /** The [FirebaseMessaging] singleton. */
        public fun getInstance(): FirebaseMessaging
    }
}
