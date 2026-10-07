/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import cocoapods.FirebaseMessaging.FIRMessaging
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseException
import platform.Foundation.NSError

/** The underlying Firebase iOS SDK object. */
public val FirebaseMessaging.ios: FIRMessaging get() = native.ios

/** @property ios The underlying Firebase iOS SDK object. */
internal actual class NativeMessaging(val ios: FIRMessaging) {
    actual fun deleteToken(): Task<Nothing?> = task { completion -> ios.deleteTokenWithCompletion { error -> completion(null, error) } }

    actual fun getToken(): Task<String> = task { completion ->
        ios.tokenWithCompletion { token, error -> completion(token ?: "", error) }
    }

    actual fun subscribeToTopic(topic: String): Task<Nothing?> = task { completion -> ios.subscribeToTopic(topic) { error -> completion(null, error) } }

    actual fun unsubscribeFromTopic(topic: String): Task<Nothing?> = task { completion -> ios.unsubscribeFromTopic(topic) { error -> completion(null, error) } }

    actual var isAutoInitEnabled: Boolean
        get() = ios.isAutoInitEnabled()
        set(value) {
            ios.autoInitEnabled = value
        }

    override fun equals(other: Any?): Boolean = other is NativeMessaging && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "FirebaseMessaging($ios)"

    actual companion object {
        actual fun getInstance(): NativeMessaging = NativeMessaging(FIRMessaging.messaging())
    }
}

internal inline fun <T> task(crossinline start: ((T, NSError?) -> Unit) -> Unit): Task<T> {
    val source = TaskCompletionSource<T>()
    start { result, error -> if (error == null) source.setResult(result) else source.setException(FirebaseException(error.localizedDescription)) }
    return source.task
}
