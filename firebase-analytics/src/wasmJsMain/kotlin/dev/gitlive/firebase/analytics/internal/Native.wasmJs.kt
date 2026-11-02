/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics.internal

import android.os.Bundle
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.analytics.externals.getAnalytics
import dev.gitlive.firebase.analytics.externals.getGoogleAnalyticsClientId
import dev.gitlive.firebase.analytics.externals.logEvent
import dev.gitlive.firebase.analytics.externals.setAnalyticsCollectionEnabled
import dev.gitlive.firebase.analytics.externals.setConsent
import dev.gitlive.firebase.analytics.externals.setDefaultEventParameters
import dev.gitlive.firebase.analytics.externals.setUserId
import dev.gitlive.firebase.analytics.externals.setUserProperties
import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.jsObject
import dev.gitlive.firebase.externals.jsSet
import dev.gitlive.firebase.externals.json
import dev.gitlive.firebase.externals.toJs
import dev.gitlive.firebase.externals.toKotlin
import dev.gitlive.firebase.externals.toKotlinString
import dev.gitlive.firebase.analytics.externals.FirebaseAnalytics as JsAnalytics

/**
 * Firebase Analytics over the `firebase/analytics` module. The JS SDK has no session timeout, session ID or analytics
 * data reset (sessions are managed by gtag), so those calls do nothing and the session ID task completes with null.
 */
internal actual class NativeAnalytics(val js: JsAnalytics) {
    actual fun logEvent(name: String, params: Bundle?): Unit = logEvent(js, name, params?.toJs())

    actual fun setDefaultEventParameters(parameters: Bundle?): Unit = setDefaultEventParameters(parameters?.toJs())

    actual fun setUserProperty(name: String, value: String?): Unit = setUserProperties(js, json(name to value))

    actual fun setConsent(consentSettings: Map<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>) {
        val settings = jsObject()
        consentSettings.forEach { (type, status) -> jsSet(settings, type.key, status.key.toJs()) }
        setConsent(settings)
    }

    actual fun setAnalyticsCollectionEnabled(enabled: Boolean): Unit = setAnalyticsCollectionEnabled(js, enabled)

    actual fun setUserId(id: String?): Unit = setUserId(js, id)

    actual fun setSessionTimeoutDuration(milliseconds: Long) {}

    actual fun getAppInstanceId(): Task<String?> {
        val source = TaskCompletionSource<String?>()
        getGoogleAnalyticsClientId(js).then(
            onFulfilled = {
                source.setResult(it.toKotlinString())
                null
            },
            onRejected = {
                source.setException(it.toThrowableOrNull() as? Exception ?: Exception(jsGet(it, "message").toKotlin()?.toString()))
                null
            },
        )
        return source.task
    }

    /** The JS SDK only provides the client ID asynchronously ([getAppInstanceId]). */
    actual fun getFirebaseInstanceId(): String? = null

    actual fun getSessionId(): Task<Long?> = TaskCompletionSource<Long?>().apply { setResult(null) }.task

    actual fun resetAnalyticsData() {}

    override fun equals(other: Any?): Boolean = other is NativeAnalytics && other.js === js

    override fun hashCode(): Int = js.hashCode()
}

/** The JS SDK defaults to the default app only when the argument is omitted, not when it is null. */
internal actual fun nativeAnalytics(app: FirebaseApp?): NativeAnalytics = NativeAnalytics(if (app == null) getAnalytics() else getAnalytics(app.js))

/** The bundle as a JS object: nested bundles as objects, bundle arrays as arrays, numbers as JS numbers. */
private fun Bundle.toJs(): JsAny? = toParameters().toJs()
