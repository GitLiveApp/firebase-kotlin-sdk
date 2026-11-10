/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.messaging

import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.messaging.DELIVERY_METRICS_ANDROID_ONLY
import dev.gitlive.firebase.messaging.NOTIFICATION_DELEGATION_ANDROID_ONLY
import dev.gitlive.firebase.messaging.REGISTER_NOT_AVAILABLE
import com.google.android.gms.tasks.TaskCompletionSource

/** firebase-java-sdk has no Cloud Messaging: every operation fails with an [UnsupportedOperationException]. */
public actual class FirebaseMessaging private constructor() {
    public actual fun deleteToken(): Task<Nothing?> = unsupported()

    public actual fun getToken(): Task<String> = unsupported()

    public actual fun subscribeToTopic(topic: String): Task<Nothing?> = unsupported()

    public actual fun unsubscribeFromTopic(topic: String): Task<Nothing?> = unsupported()

    /** Kept in memory: there is no registration to initialise on the JVM. */
    public actual var isAutoInitEnabled: Boolean = true

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
