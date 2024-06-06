package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseException

actual val Firebase.analytics: FirebaseAnalytics
    get() = TODO("Not yet implemented")

actual class FirebaseAnalytics {
    actual fun logEvent(name: String, parameters: Map<String, String>?) {}
    actual fun logEvent(name: String, block: FirebaseAnalyticsParameters.() -> Unit) {}
    actual fun setUserProperty(name: String, value: String) {}
    actual fun setUserId(id: String) {}
    actual fun resetAnalyticsData() {}
    actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {}
    actual fun setSessionTimeoutInterval(sessionTimeoutInterval: Long) {}
    actual suspend fun getSessionId(): Long? = TODO("Not yet implemented")
    actual fun setDefaultEventParameters(parameters: Map<String, String>) {}
}

actual class FirebaseAnalyticsException internal constructor(message: String) : FirebaseException(message)