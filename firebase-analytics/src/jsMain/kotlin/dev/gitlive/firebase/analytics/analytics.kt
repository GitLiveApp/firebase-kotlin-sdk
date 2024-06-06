package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.analytics.externals.getAnalytics
import dev.gitlive.firebase.analytics.externals.jsGetSessionId
import dev.gitlive.firebase.analytics.externals.jsLogEvent
import dev.gitlive.firebase.analytics.externals.jsResetAnalyticsData
import dev.gitlive.firebase.analytics.externals.jsSetAnalyticsCollectionEnabled
import dev.gitlive.firebase.analytics.externals.jsSetDefaultEventParameters
import dev.gitlive.firebase.analytics.externals.jsSetSessionTimeoutInterval
import dev.gitlive.firebase.analytics.externals.jsSetUserId
import dev.gitlive.firebase.analytics.externals.jsSetUserProperty
import kotlinx.coroutines.await

actual val Firebase.analytics: FirebaseAnalytics
    get() = FirebaseAnalytics(getAnalytics())

actual class FirebaseAnalytics(val js: dev.gitlive.firebase.analytics.externals.FirebaseAnalytics) {
    actual fun logEvent(
        name: String,
        parameters: Map<String, String>?
    ) {
        jsLogEvent(js, name, parameters)
    }

    actual fun logEvent(
        name: String,
        block: FirebaseAnalyticsParameters.() -> Unit
    ) {
        val params = FirebaseAnalyticsParameters()
        params.block()
        logEvent(name, params.parameters)
    }

    actual fun setUserProperty(name: String, value: String) {
        jsSetUserProperty(js, name, value)
    }

    actual fun setUserId(id: String) {
        jsSetUserId(js, id)
    }

    actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        jsSetAnalyticsCollectionEnabled(js, enabled)
    }

    actual fun setSessionTimeoutInterval(sessionTimeoutInterval: Long) {
        jsSetSessionTimeoutInterval(js, sessionTimeoutInterval)
    }

    actual suspend fun getSessionId(): Long? = rethrow { jsGetSessionId(js).await() }

    actual fun resetAnalyticsData() {
        jsResetAnalyticsData(js)
    }

    actual fun setDefaultEventParameters(parameters: Map<String, String>) {
        jsSetDefaultEventParameters(js, parameters)
    }
}

actual open class FirebaseAnalyticsException(code: String, cause: Throwable): FirebaseException(code, cause)

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
                FirebaseAnalyticsException(code, error)
            }
        }
    }