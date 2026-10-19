/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseException
import dev.gitlive.firebase.UnsupportedOnJs
import dev.gitlive.firebase.messaging.externals.Messaging
import dev.gitlive.firebase.messaging.externals.deleteToken
import dev.gitlive.firebase.messaging.externals.getMessaging
import dev.gitlive.firebase.messaging.externals.getToken
import kotlin.js.Promise

/** @property js The underlying Firebase JS SDK object. */
public actual class FirebaseMessaging internal constructor(public val js: Messaging) {
    public actual fun deleteToken(): Task<Nothing?> = task { deleteToken(js).then { null } }

    public actual fun getToken(): Task<String> = task { getToken(js) }

    /** A no-op: the JS SDK has no client-side topic subscription (a web token is subscribed from a server with the Admin SDK). */
    @UnsupportedOnJs
    public actual fun subscribeToTopic(topic: String): Task<Nothing?> = completedTask()

    /** A no-op: the JS SDK has no client-side topic subscription (a web token is subscribed from a server with the Admin SDK). */
    @UnsupportedOnJs
    public actual fun unsubscribeFromTopic(topic: String): Task<Nothing?> = completedTask()

    /** Always false: the JS SDK only generates a token in [getToken]. Setting it does nothing. */
    @UnsupportedOnJs
    public actual var isAutoInitEnabled: Boolean
        get() = false
        set(_) = Unit

    override fun equals(other: Any?): Boolean = other is FirebaseMessaging && other.js == js

    override fun hashCode(): Int = js.hashCode()

    override fun toString(): String = "FirebaseMessaging($js)"

    public actual companion object {
        @Deprecated("The registration token has no scope any more; getToken() returns the FCM token")
        public actual val INSTANCE_ID_SCOPE: String = "FCM"

        public actual fun getInstance(): FirebaseMessaging = rethrow { FirebaseMessaging(getMessaging()) }
    }
}

private fun completedTask(): Task<Nothing?> = TaskCompletionSource<Nothing?>().apply { setResult(null) }.task

private inline fun <T> task(start: () -> Promise<T>): Task<T> {
    val source = TaskCompletionSource<T>()
    try {
        start().then({ source.setResult(it) }, { source.setException(it.toFirebaseException()) })
    } catch (e: Throwable) {
        source.setException(e.toFirebaseException())
    }
    return source.task
}

internal inline fun <R> rethrow(function: () -> R): R = try {
    function()
} catch (e: Throwable) {
    throw e.toFirebaseException()
}

private fun Throwable.toFirebaseException(): FirebaseException = if (this is FirebaseException) this else FirebaseException(message ?: asDynamic().code.unsafeCast<String?>() ?: "Unknown error", this)
