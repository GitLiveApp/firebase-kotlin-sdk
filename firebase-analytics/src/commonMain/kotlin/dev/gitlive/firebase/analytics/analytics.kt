package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase

expect val Firebase.analytics: FirebaseAnalytics

expect class FirebaseAnalytics {
    fun logEvent(name: String, parameters: Map<String, String>? = null)
    fun logEvent(name: String, block: FirebaseAnalyticsParameters.() -> Unit)
    fun setUserProperty(name: String, value: String)
    fun setUserId(id: String)
    fun setAnalyticsCollectionEnabled(enabled: Boolean)
    fun setSessionTimeoutInterval(sessionTimeoutInterval: Long)
    suspend fun getSessionId(): Long?
    fun resetAnalyticsData()
    fun setDefaultEventParameters(parameters: Map<String, String>)
}

expect class FirebaseAnalyticsException

data class FirebaseAnalyticsParameters(
    val parameters: MutableMap<String, String> = mutableMapOf()
) {
    fun param(key: String, value: String) {
        parameters[key] = value
    }
}