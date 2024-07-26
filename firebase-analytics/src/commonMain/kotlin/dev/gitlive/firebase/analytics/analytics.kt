package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public expect val Firebase.analytics: FirebaseAnalytics

/** Returns the [FirebaseStorage] instance of a given [FirebaseApp]. */
public expect fun Firebase.analytics(app: FirebaseApp): FirebaseAnalytics

public expect class FirebaseAnalytics {
    public fun logEvent(name: String, parameters: Map<String, Any>? = null)
    public fun setUserProperty(name: String, value: String)
    public fun setUserId(id: String?)
    public fun setAnalyticsCollectionEnabled(enabled: Boolean)
    public fun setSessionTimeoutInterval(sessionTimeoutInterval: Duration)
    public suspend fun getSessionId(): Long?
    public fun resetAnalyticsData()
    public fun setDefaultEventParameters(parameters: Map<String, String>)
    public fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>)

    public enum class ConsentType {
        AD_PERSONALIZATION,
        AD_STORAGE,
        AD_USER_DATA,
        ANALYTICS_STORAGE,
    }

    public enum class ConsentStatus {
        GRANTED,
        DENIED,
    }
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

public expect class FirebaseAnalyticsException

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
