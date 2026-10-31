/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.analytics

import kotlinx.coroutines.tasks.await
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.analytics.internal.toBundle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import com.google.firebase.analytics.FirebaseAnalytics as CompatFirebaseAnalytics

/**
 * Firebase Analytics for the app it was obtained from.
 * @property compat The Android-SDK-shaped [com.google.firebase.analytics.FirebaseAnalytics] this wraps.
 */
public class FirebaseAnalytics internal constructor(public val compat: CompatFirebaseAnalytics) {
    public fun logEvent(name: String, parameters: Map<String, Any>? = null) {
        compat.logEvent(name, parameters?.toBundle())
    }

    public fun setUserProperty(name: String, value: String) {
        compat.setUserProperty(name, value)
    }

    public fun setUserId(id: String?) {
        compat.setUserId(id)
    }

    public fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        compat.setAnalyticsCollectionEnabled(enabled)
    }

    public fun setSessionTimeoutInterval(sessionTimeoutInterval: Duration) {
        compat.setSessionTimeoutDuration(sessionTimeoutInterval.inWholeMilliseconds)
    }

    public suspend fun getSessionId(): Long? = compat.getSessionId().await()

    public fun resetAnalyticsData() {
        compat.resetAnalyticsData()
    }

    public fun setDefaultEventParameters(parameters: Map<String, String>) {
        compat.setDefaultEventParameters(parameters.toBundle())
    }

    public fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>) {
        compat.setConsent(consentSettings.entries.associate { (type, status) -> type.compat to status.compat })
    }

    public enum class ConsentType(internal val compat: CompatFirebaseAnalytics.ConsentType) {
        AD_PERSONALIZATION(CompatFirebaseAnalytics.ConsentType.AD_PERSONALIZATION),
        AD_STORAGE(CompatFirebaseAnalytics.ConsentType.AD_STORAGE),
        AD_USER_DATA(CompatFirebaseAnalytics.ConsentType.AD_USER_DATA),
        ANALYTICS_STORAGE(CompatFirebaseAnalytics.ConsentType.ANALYTICS_STORAGE),
    }

    public enum class ConsentStatus(internal val compat: CompatFirebaseAnalytics.ConsentStatus) {
        GRANTED(CompatFirebaseAnalytics.ConsentStatus.GRANTED),
        DENIED(CompatFirebaseAnalytics.ConsentStatus.DENIED),
    }

    override fun equals(other: Any?): Boolean = other is FirebaseAnalytics && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = compat.toString()
}

@Deprecated("Use Kotlin Duration", replaceWith = ReplaceWith("setSessionTimeoutInterval(sessionTimeoutInterval.milliseconds)"))
public fun FirebaseAnalytics.setSessionTimeoutInterval(sessionTimeoutInterval: Long) {
    setSessionTimeoutInterval(sessionTimeoutInterval.milliseconds)
}

public fun FirebaseAnalytics.setConsent(builder: FirebaseAnalyticsConsentBuilder.() -> Unit) {
    val consentBuilder = FirebaseAnalyticsConsentBuilder()
    consentBuilder.builder()
    setConsent(consentBuilder.consentSettings)
}

public fun FirebaseAnalytics.logEvent(name: String, builder: FirebaseAnalyticsParameters.() -> Unit) {
    val params = FirebaseAnalyticsParameters()
    params.builder()
    logEvent(name, params.parameters)
}

public class FirebaseAnalyticsException(message: String) : FirebaseException(message)

public data class FirebaseAnalyticsParameters(
    val parameters: MutableMap<String, Any> = mutableMapOf(),
) {
    public fun param(key: String, value: String) {
        parameters[key] = value
    }

    public fun param(key: String, value: Double) {
        parameters[key] = value
    }

    public fun param(key: String, value: Long) {
        parameters[key] = value
    }

    public fun param(key: String, value: Int) {
        parameters[key] = value
    }

    public fun param(key: String, value: Boolean) {
        parameters[key] = value
    }
}

public data class FirebaseAnalyticsConsentBuilder(
    val consentSettings: MutableMap<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus> = mutableMapOf(),
) {
    var adPersonalization: FirebaseAnalytics.ConsentStatus?
        get() = consentSettings[FirebaseAnalytics.ConsentType.AD_PERSONALIZATION]
        set(value) {
            value?.let {
                consentSettings[FirebaseAnalytics.ConsentType.AD_PERSONALIZATION] = it
            }
        }

    var adStorage: FirebaseAnalytics.ConsentStatus?
        get() = consentSettings[FirebaseAnalytics.ConsentType.AD_STORAGE]
        set(value) {
            value?.let {
                consentSettings[FirebaseAnalytics.ConsentType.AD_STORAGE] = it
            }
        }

    var adUserData: FirebaseAnalytics.ConsentStatus?
        get() = consentSettings[FirebaseAnalytics.ConsentType.AD_USER_DATA]
        set(value) {
            value?.let {
                consentSettings[FirebaseAnalytics.ConsentType.AD_USER_DATA] = it
            }
        }

    var analyticsStorage: FirebaseAnalytics.ConsentStatus?
        get() = consentSettings[FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE]
        set(value) {
            value?.let {
                consentSettings[FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE] = it
            }
        }
}
