package dev.gitlive.firebase.analytics

import cocoapods.FirebaseAnalytics.FIRAnalytics
import cocoapods.FirebaseAnalytics.setConsent
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSError

public actual val Firebase.analytics: FirebaseAnalytics
    get() = FirebaseAnalytics(FIRAnalytics)

public actual fun Firebase.analytics(app: FirebaseApp): FirebaseAnalytics = FirebaseAnalytics(FIRAnalytics)

public actual class FirebaseAnalytics(public val ios: FIRAnalytics.Companion) {
    public actual fun logEvent(name: String, parameters: Map<String, Any>?) {
        val mappedParameters: Map<Any?, Any>? = parameters?.map { it.key to it.value }?.toMap()
        ios.logEventWithName(name, mappedParameters)
    }
    public actual fun setUserProperty(name: String, value: String) {
        ios.setUserPropertyString(value, name)
    }
    public actual fun setUserId(id: String) {
        ios.setUserID(id)
    }
    public actual fun resetAnalyticsData() {
        ios.resetAnalyticsData()
    }

    public actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        ios.setAnalyticsCollectionEnabled(enabled)
    }

    public actual fun setSessionTimeoutInterval(sessionTimeoutInterval: Long) {
        ios.setSessionTimeoutInterval(sessionTimeoutInterval.toDouble())
    }

    public actual suspend fun getSessionId(): Long? = ios.awaitResult { sessionIDWithCompletion(it) }

    public actual fun setDefaultEventParameters(parameters: Map<String, String>) {
        val mappedParameters: Map<Any?, String> = parameters.map { it.key to it.value }.toMap()
        ios.setDefaultEventParameters(mappedParameters)
    }

    public actual fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>) {
        val mappedConsentSettings: Map<Any?, *> = consentSettings.map { it.key.name to it.value.name }.toMap()
        ios.setConsent(mappedConsentSettings)
    }

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

public actual class FirebaseAnalyticsException(message: String) : FirebaseException(message)

internal suspend inline fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error ->
        if (error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(FirebaseAnalyticsException(error.toString()))
        }
    }
    job.await()
}

internal suspend inline fun <T, reified R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    function { result, error ->
        if (error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(FirebaseAnalyticsException(error.toString()))
        }
    }
    return job.await() as R
}
