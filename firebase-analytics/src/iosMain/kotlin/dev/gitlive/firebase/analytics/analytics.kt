package dev.gitlive.firebase.analytics

import cocoapods.FirebaseAnalytics.FIRAnalytics
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseException
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSError
import platform.Foundation.NSTimeInterval

actual val Firebase.analytics: FirebaseAnalytics
    get() = FirebaseAnalytics(FIRAnalytics)

actual class FirebaseAnalytics(val ios: FIRAnalytics.Companion) {
    actual fun logEvent(name: String, parameters: Map<String, String>?) {
        val mappedParameters: Map<Any?, String>? = parameters?.map {
            it.key to it.value
        }?.toMap()
        ios.logEventWithName(name, mappedParameters)
    }
    actual fun logEvent(name: String, block: FirebaseAnalyticsParameters.() -> Unit) {
        val params = FirebaseAnalyticsParameters()
        params.block()
        logEvent(name, params.parameters)
    }
    actual fun setUserProperty(name: String, value: String) {
        ios.setUserPropertyString(value, name)
    }
    actual fun setUserId(id: String) {
        ios.setUserID(id)
    }
    actual fun resetAnalyticsData() {
        ios.resetAnalyticsData()
    }

    actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        ios.setAnalyticsCollectionEnabled(enabled)
    }

    actual fun setSessionTimeoutInterval(sessionTimeoutInterval: Long) {
        ios.setSessionTimeoutInterval(sessionTimeoutInterval.toDouble())
    }

    actual suspend fun getSessionId(): Long? = ios.awaitResult { sessionIDWithCompletion(it) }

    actual fun setDefaultEventParameters(parameters: Map<String, String>) {
        val mappedParameters: Map<Any?, String> = parameters.map {
            it.key to it.value
        }.toMap()
        ios.setDefaultEventParameters(mappedParameters)
    }
}

actual class FirebaseAnalyticsException(message: String): FirebaseException(message)

suspend inline fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(FirebaseAnalyticsException(error.toString()))
        }
    }
    job.await()
}

suspend inline fun <T, reified R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    function { result, error ->
        if(error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(FirebaseAnalyticsException(error.toString()))
        }
    }
    return job.await() as R
}