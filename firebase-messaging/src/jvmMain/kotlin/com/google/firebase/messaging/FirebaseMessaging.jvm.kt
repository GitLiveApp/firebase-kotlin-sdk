/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource

/** firebase-java-sdk has no Cloud Messaging: every operation fails with an [UnsupportedOperationException]. */
public actual class FirebaseMessaging private constructor() {
    public actual fun deleteToken(): Task<Nothing?> = unsupported()

    public actual fun getToken(): Task<String> = unsupported()

    override fun toString(): String = "FirebaseMessaging"

    public actual companion object {
        @Deprecated("The registration token has no scope any more; getToken() returns the FCM token")
        @JvmField
        public actual val INSTANCE_ID_SCOPE: String = "FCM"

        private val instance = FirebaseMessaging()

        @JvmStatic
        public actual fun getInstance(): FirebaseMessaging = instance
    }
}

internal const val MESSAGING_UNSUPPORTED = "Firebase Cloud Messaging is not available on the JVM"

internal fun <T> unsupported(): Task<T> = TaskCompletionSource<T>().apply { setException(UnsupportedOperationException(MESSAGING_UNSUPPORTED)) }.task
