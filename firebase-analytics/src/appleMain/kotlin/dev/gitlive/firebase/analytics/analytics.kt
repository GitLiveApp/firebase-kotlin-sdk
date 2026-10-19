package dev.gitlive.firebase.analytics

import cocoapods.FirebaseAnalytics.FIRAnalytics
import cocoapods.FirebaseAnalytics.setConsent
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import kotlin.time.Duration
import kotlin.time.DurationUnit

public actual val Firebase.analytics: FirebaseAnalytics
    get() = FirebaseAnalytics(FIRAnalytics)

public actual fun Firebase.analytics(app: FirebaseApp): FirebaseAnalytics = FirebaseAnalytics(FIRAnalytics)

public val FirebaseAnalytics.ios: FIRAnalytics.Companion get() = ios

public actual class FirebaseAnalytics(internal val ios: FIRAnalytics.Companion) {
    public actual fun logEvent(name: String, parameters: Map<String, Any>?) {
        val mappedParameters: Map<Any?, Any>? = parameters?.mapValues { (_, value) -> value.toFirebaseParameterValue() }
        ios.logEventWithName(name, mappedParameters)
    }
    public actual fun setUserProperty(name: String, value: String) {
        ios.setUserPropertyString(value, name)
    }
    public actual fun setUserId(id: String?) {
        ios.setUserID(id)
    }
    public actual fun resetAnalyticsData() {
        ios.resetAnalyticsData()
    }

    public actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        ios.setAnalyticsCollectionEnabled(enabled)
    }

    public actual fun setSessionTimeoutInterval(sessionTimeoutInterval: Duration) {
        ios.setSessionTimeoutInterval(sessionTimeoutInterval.toDouble(DurationUnit.SECONDS))
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

/**
 * `logEventWithName(name:parameters:)` previously received boxed Kotlin values as-is. Kotlin/Native
 * bridges a boxed numeric/boolean value to Objective-C as its own NSNumber-conforming subclass
 * rather than a genuine CoreFoundation-backed NSNumber, and Firebase's own parameter validation has
 * been observed silently dropping a `Long` parameter passed this way while an `Int` of the same
 * value survives - confirmed against a production Firebase project's BigQuery export, where a
 * `Long` param logged this way never once arrived on iOS while the same value coerced to `Int`
 * did. Explicitly constructing an [NSNumber] here produces a real CoreFoundation-backed instance
 * regardless of the source Kotlin type, matching what `logEventWithName` receives from
 * Swift/Objective-C callers.
 */
internal fun Any.toFirebaseParameterValue(): Any = when (this) {
    is Long -> NSNumber(longLong = this)
    is Int -> NSNumber(int = this)
    is Short -> NSNumber(short = this)
    is Byte -> NSNumber(char = this)
    is Double -> NSNumber(double = this)
    is Float -> NSNumber(float = this)
    is Boolean -> NSNumber(bool = this)
    else -> this
}

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
