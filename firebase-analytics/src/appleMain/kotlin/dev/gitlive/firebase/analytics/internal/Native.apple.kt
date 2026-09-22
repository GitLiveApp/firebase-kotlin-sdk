/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics.internal

import android.os.Bundle
import cocoapods.FirebaseAnalytics.FIRAnalytics
import cocoapods.FirebaseAnalytics.setConsent
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.analytics.FirebaseAnalytics
import platform.Foundation.NSError

/** Firebase Analytics over `FIRAnalytics`, whose API is static: there is one client per process. */
internal actual class NativeAnalytics(val ios: FIRAnalytics.Companion) {
    actual fun logEvent(name: String, params: Bundle?): Unit = ios.logEventWithName(name, params?.toParameters()?.toIos())

    actual fun setDefaultEventParameters(parameters: Bundle?): Unit = ios.setDefaultEventParameters(parameters?.toParameters()?.toIos())

    actual fun setUserProperty(name: String, value: String?): Unit = ios.setUserPropertyString(value, name)

    actual fun setConsent(consentSettings: Map<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>): Unit = ios.setConsent(consentSettings.entries.associate<_, Any?, Any?> { (type, status) -> type.key to status.key })

    actual fun setAnalyticsCollectionEnabled(enabled: Boolean): Unit = ios.setAnalyticsCollectionEnabled(enabled)

    actual fun setUserId(id: String?): Unit = ios.setUserID(id)

    actual fun setSessionTimeoutDuration(milliseconds: Long): Unit = ios.setSessionTimeoutInterval(milliseconds / 1000.0)

    actual fun getAppInstanceId(): Task<String?> = TaskCompletionSource<String?>().apply { setResult(ios.appInstanceID()) }.task

    actual fun getFirebaseInstanceId(): String? = ios.appInstanceID()

    actual fun getSessionId(): Task<Long?> {
        val source = TaskCompletionSource<Long?>()
        ios.sessionIDWithCompletion { sessionId, error: NSError? ->
            if (error == null) source.setResult(sessionId) else source.setException(FirebaseException(error.localizedDescription))
        }
        return source.task
    }

    actual fun resetAnalyticsData(): Unit = ios.resetAnalyticsData()

    override fun equals(other: Any?): Boolean = other is NativeAnalytics

    override fun hashCode(): Int = 0
}

private val instance = NativeAnalytics(FIRAnalytics)

internal actual fun nativeAnalytics(app: FirebaseApp?): NativeAnalytics = instance

private fun Map<String, Any?>.toIos(): Map<Any?, Any?> = entries.associate<_, Any?, Any?> { (key, value) -> key to value }
