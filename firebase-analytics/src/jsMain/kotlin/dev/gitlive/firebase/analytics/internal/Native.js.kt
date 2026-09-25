/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics.internal

import android.os.Bundle
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.analytics.externals.ConsentSettings
import dev.gitlive.firebase.analytics.externals.getAnalytics
import dev.gitlive.firebase.analytics.externals.getGoogleAnalyticsClientId
import dev.gitlive.firebase.analytics.externals.logEvent
import dev.gitlive.firebase.analytics.externals.setAnalyticsCollectionEnabled
import dev.gitlive.firebase.analytics.externals.setConsent
import dev.gitlive.firebase.analytics.externals.setDefaultEventParameters
import dev.gitlive.firebase.analytics.externals.setUserId
import dev.gitlive.firebase.analytics.externals.setUserProperties
import kotlin.js.Json
import kotlin.js.json
import dev.gitlive.firebase.analytics.externals.FirebaseAnalytics as JsAnalytics

/**
 * Firebase Analytics over the `firebase/analytics` module. The JS SDK has no session timeout, session ID or analytics
 * data reset (sessions are managed by gtag), so those calls do nothing and the session ID task completes with null.
 */
internal actual class NativeAnalytics(val js: JsAnalytics) {
    actual fun logEvent(name: String, params: Bundle?): Unit = logEvent(js, name, params?.toJson())

    actual fun setDefaultEventParameters(parameters: Bundle?): Unit = setDefaultEventParameters(parameters?.toJson())

    actual fun setUserProperty(name: String, value: String?): Unit = setUserProperties(js, json(name to value))

    actual fun setConsent(consentSettings: Map<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>) {
        val settings = json().unsafeCast<ConsentSettings>()
        consentSettings.forEach { (type, status) -> settings.asDynamic()[type.key] = status.key }
        setConsent(settings)
    }

    actual fun setAnalyticsCollectionEnabled(enabled: Boolean): Unit = setAnalyticsCollectionEnabled(js, enabled)

    actual fun setUserId(id: String?): Unit = setUserId(js, id)

    actual fun setSessionTimeoutDuration(milliseconds: Long) {}

    actual fun getAppInstanceId(): Task<String?> {
        val source = TaskCompletionSource<String?>()
        getGoogleAnalyticsClientId(js).then({ source.setResult(it) }, { source.setException(it as? Exception ?: Exception(it.message, it)) })
        return source.task
    }

    /** The JS SDK only provides the client ID asynchronously ([getAppInstanceId]). */
    actual fun getFirebaseInstanceId(): String? = null

    actual fun getSessionId(): Task<Long?> = TaskCompletionSource<Long?>().apply { setResult(null) }.task

    actual fun resetAnalyticsData() {}

    override fun equals(other: Any?): Boolean = other is NativeAnalytics && other.js === js

    override fun hashCode(): Int = js.hashCode()
}

internal actual fun nativeAnalytics(app: FirebaseApp?): NativeAnalytics = NativeAnalytics(getAnalytics(app?.js))

/** The bundle as a JS object: nested bundles as objects, bundle arrays as arrays, numbers as JS numbers. */
private fun Bundle.toJson(): Json = json(*toParameters().map { (key, value) -> key to value.toJs() }.toTypedArray())

private fun Any?.toJs(): Any? = when (this) {
    is Long -> toDouble()
    is Map<*, *> -> json(*entries.map { (k, v) -> k.toString() to v.toJs() }.toTypedArray())
    is List<*> -> map { it.toJs() }.toTypedArray()
    else -> this
}
