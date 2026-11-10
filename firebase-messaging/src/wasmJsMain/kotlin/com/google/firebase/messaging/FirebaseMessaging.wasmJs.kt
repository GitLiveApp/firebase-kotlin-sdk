/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseException
import dev.gitlive.firebase.UnsupportedOnJs
import dev.gitlive.firebase.messaging.DELIVERY_METRICS_ANDROID_ONLY
import dev.gitlive.firebase.messaging.NOTIFICATION_DELEGATION_ANDROID_ONLY
import dev.gitlive.firebase.messaging.REGISTER_NOT_AVAILABLE
import dev.gitlive.firebase.externals.errorCode
import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.toKotlin
import dev.gitlive.firebase.externals.toKotlinString
import dev.gitlive.firebase.messaging.externals.Messaging
import kotlin.js.JsException
import kotlin.js.Promise
import dev.gitlive.firebase.messaging.externals.deleteToken as jsDeleteToken
import dev.gitlive.firebase.messaging.externals.getMessaging as jsGetMessaging
import dev.gitlive.firebase.messaging.externals.getToken as jsGetToken

/** @property js The underlying Firebase JS SDK object. */
public actual class FirebaseMessaging internal constructor(public val js: Messaging) {
    public actual fun deleteToken(): Task<Nothing?> = task({ jsDeleteToken(js) }) { null }

    public actual fun getToken(): Task<String> = task({ jsGetToken(js) }) { it.toKotlinString() }

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

    @Deprecated(DELIVERY_METRICS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual fun deliveryMetricsExportToBigQueryEnabled(): Boolean = throw UnsupportedOperationException(DELIVERY_METRICS_ANDROID_ONLY)

    @Deprecated(DELIVERY_METRICS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual fun setDeliveryMetricsExportToBigQuery(enable: Boolean): Unit = throw UnsupportedOperationException(DELIVERY_METRICS_ANDROID_ONLY)

    @Deprecated(NOTIFICATION_DELEGATION_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual fun isNotificationDelegationEnabled(): Boolean = throw UnsupportedOperationException(NOTIFICATION_DELEGATION_ANDROID_ONLY)

    @Deprecated(NOTIFICATION_DELEGATION_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public actual fun setNotificationDelegationEnabled(enable: Boolean): Task<Nothing?> = throw UnsupportedOperationException(NOTIFICATION_DELEGATION_ANDROID_ONLY)

    @Deprecated(REGISTER_NOT_AVAILABLE, level = DeprecationLevel.ERROR)
    public actual fun register(): Task<Nothing?> = throw UnsupportedOperationException(REGISTER_NOT_AVAILABLE)

    @Deprecated(REGISTER_NOT_AVAILABLE, level = DeprecationLevel.ERROR)
    public actual fun unregister(): Task<Nothing?> = throw UnsupportedOperationException(REGISTER_NOT_AVAILABLE)

    override fun equals(other: Any?): Boolean = other is FirebaseMessaging && other.js == js

    override fun hashCode(): Int = js.hashCode()

    override fun toString(): String = "FirebaseMessaging($js)"

    public actual companion object {
        @Deprecated("The registration token has no scope any more; getToken() returns the FCM token")
        public actual val INSTANCE_ID_SCOPE: String = "FCM"

        public actual fun getInstance(): FirebaseMessaging = rethrow { FirebaseMessaging(jsGetMessaging()) }
    }
}

private fun completedTask(): Task<Nothing?> = TaskCompletionSource<Nothing?>().apply { setResult(null) }.task

private inline fun <T : JsAny?, R> task(start: () -> Promise<T>, crossinline result: (T) -> R): Task<R> {
    val source = TaskCompletionSource<R>()
    try {
        start().then(
            onFulfilled = {
                source.setResult(result(it))
                null
            },
            onRejected = {
                source.setException(it.toFirebaseException())
                null
            },
        )
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

private fun Throwable.toFirebaseException(): FirebaseException = when (this) {
    is FirebaseException -> this
    is JsException -> FirebaseException(thrownValue?.let { jsGet(it, "message") }.toKotlin()?.toString() ?: errorCode() ?: message ?: "Unknown error", this)
    else -> FirebaseException(message ?: "Unknown error", this)
}

/** A promise rejection carries the JS error itself rather than a [Throwable]. */
private fun JsAny.toFirebaseException(): FirebaseException {
    val message = jsGet(this, "message").toKotlin()?.toString() ?: jsGet(this, "code").toKotlin()?.toString() ?: "Unknown error"
    return FirebaseException(message, toThrowableOrNull() ?: Exception(message))
}
