/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.analytics

import com.google.firebase.Firebase
import dev.gitlive.firebase.analytics.internal.compatAnalytics

/*
 * The `AnalyticsKt` file facade of the Firebase Android SDK (`Firebase.analytics`, `logEvent { }`, `setConsent { }`).
 * On Android it is a header stub for the real facade; elsewhere it is real code.
 */

/** The [FirebaseAnalytics] of the default app. */
public val Firebase.analytics: FirebaseAnalytics
    get() = compatAnalytics(null)

/** Logs [name] with the parameters set in [block], e.g. `logEvent(FirebaseAnalytics.Event.LOGIN) { param(FirebaseAnalytics.Param.METHOD, "email") }`. */
public inline fun FirebaseAnalytics.logEvent(name: String, block: ParametersBuilder.() -> Unit) {
    logEvent(name, ParametersBuilder().apply(block).bundle)
}

/** Sets the consent settings assigned in [block], e.g. `setConsent { adStorage = FirebaseAnalytics.ConsentStatus.GRANTED }`. */
public inline fun FirebaseAnalytics.setConsent(block: ConsentBuilder.() -> Unit) {
    setConsent(ConsentBuilder().apply(block).asMap())
}
