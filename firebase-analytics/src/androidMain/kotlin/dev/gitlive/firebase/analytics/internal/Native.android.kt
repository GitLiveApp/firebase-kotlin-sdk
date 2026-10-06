/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics.internal

import android.os.Bundle
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import dev.gitlive.firebase.analytics.stub

/** Unused on Android, where [FirebaseAnalytics] is a header stub for the real class. */
internal actual class NativeAnalytics {
    actual fun logEvent(name: String, params: Bundle?): Unit = stub()
    actual fun setDefaultEventParameters(parameters: Bundle?): Unit = stub()
    actual fun setUserProperty(name: String, value: String?): Unit = stub()
    actual fun setConsent(consentSettings: Map<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>): Unit = stub()
    actual fun setAnalyticsCollectionEnabled(enabled: Boolean): Unit = stub()
    actual fun setUserId(id: String?): Unit = stub()
    actual fun setSessionTimeoutDuration(milliseconds: Long): Unit = stub()
    actual fun getAppInstanceId(): Task<String?> = stub()
    actual fun getFirebaseInstanceId(): String? = stub()
    actual fun getSessionId(): Task<Long?> = stub()
    actual fun resetAnalyticsData(): Unit = stub()
}

internal actual fun nativeAnalytics(app: FirebaseApp?): NativeAnalytics = stub()

/** Analytics is per process on Android; `Firebase.analytics` is the real SDK's `AnalyticsKt.getAnalytics`. */
internal actual fun compatAnalytics(app: FirebaseApp?): FirebaseAnalytics = com.google.firebase.Firebase.analytics

internal actual fun Map<String, Any?>.toBundle(): Bundle = Bundle().also { bundle ->
    forEach { (key, value) ->
        when (value) {
            null -> bundle.putString(key, null)
            is String -> bundle.putString(key, value)
            is Int -> bundle.putInt(key, value)
            is Long -> bundle.putLong(key, value)
            is Float -> bundle.putDouble(key, value.toDouble())
            is Double -> bundle.putDouble(key, value)
            is Boolean -> bundle.putBoolean(key, value)
            is Bundle -> bundle.putBundle(key, value)
            is Map<*, *> -> bundle.putBundle(key, value.entries.associate { (k, v) -> k.toString() to v }.toBundle())
            else -> throw IllegalArgumentException("Unsupported analytics parameter type ${value::class} for $key")
        }
    }
}
