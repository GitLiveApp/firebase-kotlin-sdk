package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException

actual val Firebase.analytics: FirebaseAnalytics
    get() = TODO("Not yet implemented")

actual fun Firebase.analytics(app: FirebaseApp): FirebaseAnalytics {
    TODO("Not yetimplemented")
}

actual class FirebaseAnalytics {
    actual fun setUserProperty(name: String, value: String) {}
    actual fun setUserId(id: String) {}
    actual fun resetAnalyticsData() {}
    actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {}
    actual fun setSessionTimeoutInterval(sessionTimeoutInterval: Long) {}
    actual suspend fun getSessionId(): Long? = TODO("Not yet implemented")
    actual fun setDefaultEventParameters(parameters: Map<String, String>) {}
    actual fun logEvent(name: String, parameters: Map<String, Any>?) {}

    actual fun setConsent(consentSettings: Map<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>) {}

    actual enum class ConsentType {
        AD_PERSONALIZATION, AD_STORAGE, AD_USER_DATA, ANALYTICS_STORAGE
    }

    actual enum class ConsentStatus {
        GRANTED, DENIED
    }
}

actual class FirebaseAnalyticsException internal constructor(message: String) : FirebaseException(message)