/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics.internal

import android.os.Bundle
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

/** firebase-java-sdk has no analytics: every call is a no-op and the tasks complete with null. */
internal actual class NativeAnalytics {
    actual fun logEvent(name: String, params: Bundle?) {}
    actual fun setDefaultEventParameters(parameters: Bundle?) {}
    actual fun setUserProperty(name: String, value: String?) {}
    actual fun setConsent(consentSettings: Map<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>) {}
    actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {}
    actual fun setUserId(id: String?) {}
    actual fun setSessionTimeoutDuration(milliseconds: Long) {}
    actual fun getAppInstanceId(): Task<String?> = TaskCompletionSource<String?>().apply { setResult(null) }.task
    actual fun getFirebaseInstanceId(): String? = null
    actual fun getSessionId(): Task<Long?> = TaskCompletionSource<Long?>().apply { setResult(null) }.task
    actual fun resetAnalyticsData() {}
}

private val instance = NativeAnalytics()

internal actual fun nativeAnalytics(app: FirebaseApp?): NativeAnalytics = instance

/** firebase-java-sdk's Bundle is built from a map; the parameters are never read on the JVM. */
internal actual fun Map<String, Any?>.toBundle(): Bundle = Bundle(this)
