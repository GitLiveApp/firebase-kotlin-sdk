package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.analytics.externals.getAnalytics
import dev.gitlive.firebase.js
import kotlin.js.json
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
        val json = json(*parameters?.map { it.key to it.value }.orEmpty().toTypedArray())
        dev.gitlive.firebase.analytics.externals.logEvent(js, name, json)
    }

    public actual fun setUserProperty(name: String, value: String) {
        dev.gitlive.firebase.analytics.externals.setUserProperties(js, json(name to value))
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
        dev.gitlive.firebase.analytics.externals.setDefaultEventParameters(json(*parameters.map { it.key to it.value }.toTypedArray()))
    }

    public actual fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>) {
        val consent = json(
            *consentSettings.map { (type, status) ->
                type.name.lowercase() to status.name.lowercase()
            }.toTypedArray(),
        )
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

public actual open class FirebaseAnalyticsException(code: String, cause: Throwable) : FirebaseException(code, cause)

internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch (e: dynamic) {
        throw errorToException(e)
    }
}

internal fun errorToException(error: dynamic) = (error?.code ?: error?.message ?: "")
    .toString()
    .lowercase()
    .let { code ->
        when {
            else -> {
                println("Unknown error code in ${JSON.stringify(error)}")
                FirebaseAnalyticsException(code, error.unsafeCast<Throwable>())
            }
        }
    }
