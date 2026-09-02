package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.analytics.externals.getAnalytics
import dev.gitlive.firebase.externals.errorCode
import dev.gitlive.firebase.externals.jsObject
import dev.gitlive.firebase.externals.jsSet
import dev.gitlive.firebase.externals.stringifyThrownValue
import dev.gitlive.firebase.externals.toJs
import dev.gitlive.firebase.js
import kotlin.js.JsException
import kotlin.time.Duration

public actual val Firebase.analytics: FirebaseAnalytics
    get() = FirebaseAnalytics(getAnalytics())

public actual fun Firebase.analytics(app: FirebaseApp): FirebaseAnalytics = FirebaseAnalytics(getAnalytics(app.js))

public val FirebaseAnalytics.js: dev.gitlive.firebase.analytics.externals.FirebaseAnalytics get() = js

public actual class FirebaseAnalytics(internal val js: dev.gitlive.firebase.analytics.externals.FirebaseAnalytics) {
    public actual fun logEvent(
        name: String,
        parameters: Map<String, Any>?,
    ) {
        dev.gitlive.firebase.analytics.externals.logEvent(js, name, parameters?.toJsObject())
    }

    public actual fun setUserProperty(name: String, value: String) {
        dev.gitlive.firebase.analytics.externals.setUserProperties(js, mapOf(name to value).toJsObject())
    }

    public actual fun setUserId(id: String?) {
        dev.gitlive.firebase.analytics.externals.setUserId(js, id)
    }

    public actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        dev.gitlive.firebase.analytics.externals.setAnalyticsCollectionEnabled(js, enabled)
    }

    public actual fun setSessionTimeoutInterval(sessionTimeoutInterval: Duration): Unit = throw UnsupportedOperationException("Setting the session timeout is not supported in the Firebase JS SDK")

    public actual suspend fun getSessionId(): Long? = throw UnsupportedOperationException("Getting the session ID is not supported in the Firebase JS SDK")

    public actual fun resetAnalyticsData(): Unit = throw UnsupportedOperationException("Resetting analytics data is not supported in the Firebase JS SDK")

    public actual fun setDefaultEventParameters(parameters: Map<String, String>) {
        dev.gitlive.firebase.analytics.externals.setDefaultEventParameters(parameters.toJsObject())
    }

    public actual fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>) {
        val consent = jsObject()
        consentSettings.forEach {
            when (it.key) {
                ConsentType.AD_PERSONALIZATION -> jsSet(consent, "ad_personalization", it.value.name.lowercase().toJs())
                ConsentType.AD_STORAGE -> jsSet(consent, "ad_storage", it.value.name.lowercase().toJs())
                ConsentType.AD_USER_DATA -> jsSet(consent, "ad_user_data", it.value.name.lowercase().toJs())
                ConsentType.ANALYTICS_STORAGE -> jsSet(consent, "analytics_storage", it.value.name.lowercase().toJs())
            }
        }
        dev.gitlive.firebase.analytics.externals.setConsent(consent)
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

private fun Map<String, *>.toJsObject(): JsAny {
    val obj = jsObject()
    forEach { (key, value) -> jsSet(obj, key, value.toJs()) }
    return obj
}

public actual open class FirebaseAnalyticsException(code: String, cause: Throwable) : FirebaseException(code, cause)

internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: JsException) {
        throw errorToException(e)
    }
}

internal fun errorToException(cause: JsException): FirebaseAnalyticsException {
    val code = (cause.errorCode() ?: cause.message ?: "").lowercase()
    println("Unknown error code in ${cause.stringifyThrownValue()}")
    return FirebaseAnalyticsException(code, cause)
}
