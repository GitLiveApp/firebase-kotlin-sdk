/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import cocoapods.FirebaseMessaging.FIRMessaging
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseException
import platform.Foundation.NSError

/** @property ios The underlying Firebase iOS SDK object. */
public actual class FirebaseMessaging internal constructor(public val ios: FIRMessaging) {
    public actual fun deleteToken(): Task<Nothing?> = task { completion -> ios.deleteTokenWithCompletion { error -> completion(null, error) } }

    public actual fun getToken(): Task<String> = task { completion ->
        ios.tokenWithCompletion { token, error -> completion(token ?: "", error) }
    }

    override fun equals(other: Any?): Boolean = other is FirebaseMessaging && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "FirebaseMessaging($ios)"

    public actual companion object {
        @Deprecated("The registration token has no scope any more; getToken() returns the FCM token")
        public actual val INSTANCE_ID_SCOPE: String = "FCM"

        public actual fun getInstance(): FirebaseMessaging = FirebaseMessaging(FIRMessaging.messaging())
    }
}

internal inline fun <T> task(crossinline start: ((T, NSError?) -> Unit) -> Unit): Task<T> {
    val source = TaskCompletionSource<T>()
    start { result, error -> if (error == null) source.setResult(result) else source.setException(FirebaseException(error.localizedDescription)) }
    return source.task
}
