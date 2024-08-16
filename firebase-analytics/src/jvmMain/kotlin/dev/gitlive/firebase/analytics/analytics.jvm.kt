package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlin.time.Duration

public actual val Firebase.analytics: FirebaseAnalytics
    get() = TODO("Not yet implemented")

public actual fun Firebase.analytics(app: FirebaseApp): FirebaseAnalytics {
    TODO("Not yet implemented")
}

public actual class FirebaseAnalytics {
    public actual fun setUserProperty(name: String, value: String) {}
    public actual fun setUserId(id: String?) {}
    public actual fun resetAnalyticsData() {}
    public actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {}
    public actual fun setSessionTimeoutInterval(sessionTimeoutInterval: Duration) {}
    public actual suspend fun getSessionId(): Long? = TODO("Not yet implemented")
    public actual fun setDefaultEventParameters(parameters: Map<String, String>) {}
    public actual fun logEvent(name: String, parameters: Map<String, Any>?) {}

    public actual fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>) {}

    public actual enum class ConsentType {
        AD_PERSONALIZATION,
        AD_STORAGE,
        AD_USER_DATA,
        ANALYTICS_STORAGE,
    }

    public actual enum class ConsentStatus {
        GRANTED,
        DENIED,
    }
}

public actual class FirebaseAnalyticsException internal constructor(message: String) : FirebaseException(message)
