/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import dev.gitlive.firebase.messaging.DELIVERY_METRICS_ANDROID_ONLY
import dev.gitlive.firebase.messaging.NOTIFICATION_DELEGATION_ANDROID_ONLY
import dev.gitlive.firebase.messaging.REGISTER_NOT_AVAILABLE
import swiftPMImport.dev.gitlive.firebase.messaging.FIRMessaging
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

    public actual fun subscribeToTopic(topic: String): Task<Nothing?> = task { completion -> ios.subscribeToTopic(topic) { error -> completion(null, error) } }

    public actual fun unsubscribeFromTopic(topic: String): Task<Nothing?> = task { completion -> ios.unsubscribeFromTopic(topic) { error -> completion(null, error) } }

    public actual var isAutoInitEnabled: Boolean
        get() = ios.isAutoInitEnabled()
        set(value) {
            ios.autoInitEnabled = value
        }

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
