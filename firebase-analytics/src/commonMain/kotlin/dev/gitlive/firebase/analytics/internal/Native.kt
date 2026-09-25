/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics.internal

import android.os.Bundle
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * The platform analytics client behind [FirebaseAnalytics] on the platforms where that class is real code (Apple, JS
 * and the JVM, where it is a no-op as firebase-java-sdk has no analytics). On Android [FirebaseAnalytics] is a header
 * stub for the real class, so this is never used there.
 */
internal expect class NativeAnalytics {
    fun logEvent(name: String, params: Bundle?)
    fun setDefaultEventParameters(parameters: Bundle?)
    fun setUserProperty(name: String, value: String?)
    fun setConsent(consentSettings: Map<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>)
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
    fun setUserId(id: String?)
    fun setSessionTimeoutDuration(milliseconds: Long)
    fun getAppInstanceId(): Task<String?>
    fun getFirebaseInstanceId(): String?
    fun getSessionId(): Task<Long?>
    fun resetAnalyticsData()
}

/** The analytics client of [app], or of the default app when null. */
internal expect fun nativeAnalytics(app: FirebaseApp?): NativeAnalytics

/** The [FirebaseAnalytics] of [app]: the single instance on Android and Apple, one per app on JS. */
internal expect fun compatAnalytics(app: FirebaseApp?): FirebaseAnalytics

/** An Android `Bundle` of these parameters (strings, numbers, booleans, nested maps and bundles, arrays of bundles). */
internal expect fun Map<String, Any?>.toBundle(): Bundle

/** The Google Analytics key of a consent type (`ad_storage`), as the Apple and JS SDKs take it. */
internal val FirebaseAnalytics.ConsentType.key: String
    get() = when (this) {
        FirebaseAnalytics.ConsentType.AD_STORAGE -> "ad_storage"
        FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE -> "analytics_storage"
        FirebaseAnalytics.ConsentType.AD_USER_DATA -> "ad_user_data"
        FirebaseAnalytics.ConsentType.AD_PERSONALIZATION -> "ad_personalization"
    }

/** The Google Analytics value of a consent status (`granted`), as the Apple and JS SDKs take it. */
internal val FirebaseAnalytics.ConsentStatus.key: String
    get() = when (this) {
        FirebaseAnalytics.ConsentStatus.GRANTED -> "granted"
        FirebaseAnalytics.ConsentStatus.DENIED -> "denied"
    }

internal const val GET_INSTANCE_ANDROID_ONLY =
    "getInstance takes an android.content.Context and can only be called from Android code; use Firebase.analytics from common code"
