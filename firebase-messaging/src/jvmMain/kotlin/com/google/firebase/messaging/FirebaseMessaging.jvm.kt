/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource

/** firebase-java-sdk has no Cloud Messaging: every operation fails with an [UnsupportedOperationException]. */
internal actual class NativeMessaging private constructor() {
    actual fun deleteToken(): Task<Nothing?> = unsupported()

    actual fun getToken(): Task<String> = unsupported()

    actual fun subscribeToTopic(topic: String): Task<Nothing?> = unsupported()

    actual fun unsubscribeFromTopic(topic: String): Task<Nothing?> = unsupported()

    /** Kept in memory: there is no registration to initialise on the JVM. */
    actual var isAutoInitEnabled: Boolean = true

    override fun toString(): String = "FirebaseMessaging"

    actual companion object {
        private val instance = NativeMessaging()

        actual fun getInstance(): NativeMessaging = instance
    }
}

internal const val MESSAGING_UNSUPPORTED = "Firebase Cloud Messaging is not available on the JVM"

internal fun <T> unsupported(): Task<T> = TaskCompletionSource<T>().apply { setException(UnsupportedOperationException(MESSAGING_UNSUPPORTED)) }.task
