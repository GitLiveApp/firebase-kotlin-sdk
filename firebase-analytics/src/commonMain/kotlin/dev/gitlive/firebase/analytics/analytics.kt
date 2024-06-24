package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp

expect val Firebase.analytics: FirebaseAnalytics

/** Returns the [FirebaseStorage] instance of a given [FirebaseApp]. */
expect fun Firebase.analytics(app: FirebaseApp): FirebaseAnalytics

expect class FirebaseAnalytics {
    fun logEvent(name: String, parameters: Map<String, Any>? = null)
    fun setUserProperty(name: String, value: String)
    fun setUserId(id: String)
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
    fun setSessionTimeoutInterval(sessionTimeoutInterval: Long)
    suspend fun getSessionId(): Long?
    fun resetAnalyticsData()
    fun setDefaultEventParameters(parameters: Map<String, String>)
    fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>)

    enum class ConsentType {
        AD_PERSONALIZATION,
        AD_STORAGE,
        AD_USER_DATA,
        ANALYTICS_STORAGE,
    }

    enum class ConsentStatus {
        GRANTED,
        DENIED,
    }
}

fun FirebaseAnalytics.setConsent(builder: FirebaseAnalyticsConsentBuilder.() -> Unit) {
    val consentBuilder = FirebaseAnalyticsConsentBuilder()
    consentBuilder.builder()
    setConsent(consentBuilder.consentSettings)
}

fun FirebaseAnalytics.logEvent(name: String, builder: FirebaseAnalyticsParameters.() -> Unit) {
    val params = FirebaseAnalyticsParameters()
    params.builder()
    logEvent(name, params.parameters)
}

expect class FirebaseAnalyticsException

data class FirebaseAnalyticsParameters(
    val parameters: MutableMap<String, Any> = mutableMapOf(),
) {
    fun param(key: String, value: String) {
        parameters[key] = value
    }

    fun param(key: String, value: Double) {
        parameters[key] = value
    }

    fun param(key: String, value: Long) {
        parameters[key] = value
    }

    fun param(key: String, value: Int) {
        parameters[key] = value
    }

    fun param(key: String, value: Boolean) {
        parameters[key] = value
    }
}

data class FirebaseAnalyticsConsentBuilder(
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
