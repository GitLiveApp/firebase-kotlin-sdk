/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.analytics

import android.os.Bundle
import dev.gitlive.firebase.analytics.internal.toBundle

/** The parameters of an event logged with [logEvent], mirroring `com.google.firebase.analytics.ParametersBuilder`. */
public class ParametersBuilder {
    private val parameters = linkedMapOf<String, Any?>()

    /** The parameters as a [Bundle]. */
    public val bundle: Bundle
        get() = parameters.toBundle()

    public fun param(key: String, value: Double) {
        parameters[key] = value
    }

    public fun param(key: String, value: Long) {
        parameters[key] = value
    }

    public fun param(key: String, value: String) {
        parameters[key] = value
    }

    public fun param(key: String, value: Bundle) {
        parameters[key] = value
    }

    /** An array of bundles, such as the [FirebaseAnalytics.Param.ITEMS] of an e-commerce event. */
    public fun param(key: String, value: Array<Bundle>) {
        parameters[key] = value
    }
}

/** The consent settings set with [setConsent], mirroring `com.google.firebase.analytics.ConsentBuilder`. */
public class ConsentBuilder {
    public var adStorage: FirebaseAnalytics.ConsentStatus? = null
    public var analyticsStorage: FirebaseAnalytics.ConsentStatus? = null
    public var adUserData: FirebaseAnalytics.ConsentStatus? = null
    public var adPersonalization: FirebaseAnalytics.ConsentStatus? = null

    /** The settings that were assigned. */
    public fun asMap(): Map<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus> = buildMap {
        adStorage?.let { put(FirebaseAnalytics.ConsentType.AD_STORAGE, it) }
        analyticsStorage?.let { put(FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE, it) }
        adUserData?.let { put(FirebaseAnalytics.ConsentType.AD_USER_DATA, it) }
        adPersonalization?.let { put(FirebaseAnalytics.ConsentType.AD_PERSONALIZATION, it) }
    }
}
