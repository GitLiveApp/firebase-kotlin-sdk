package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.analytics.externals.getAnalytics
import kotlinx.coroutines.await

actual val Firebase.analytics: FirebaseAnalytics
    get() = FirebaseAnalytics(getAnalytics())

actual fun Firebase.analytics(app: FirebaseApp) =
    FirebaseAnalytics(getAnalytics(app.js))

actual class FirebaseAnalytics(val js: dev.gitlive.firebase.analytics.externals.FirebaseAnalytics) {
    actual fun logEvent(
        name: String,
        parameters: Map<String, Any>?
    ) {
        dev.gitlive.firebase.analytics.externals.logEvent(js, name, parameters)
    }

    actual fun setUserProperty(name: String, value: String) {
        dev.gitlive.firebase.analytics.externals.setUserProperty(js, name, value)
    }

    actual fun setUserId(id: String) {
        dev.gitlive.firebase.analytics.externals.setUserId(js, id)
    }

    actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        dev.gitlive.firebase.analytics.externals.setAnalyticsCollectionEnabled(js, enabled)
    }

    actual fun setSessionTimeoutInterval(sessionTimeoutInterval: Long) {
        dev.gitlive.firebase.analytics.externals.setSessionTimeoutInterval(js, sessionTimeoutInterval)
    }

    actual suspend fun getSessionId(): Long? = rethrow { dev.gitlive.firebase.analytics.externals.getSessionId(js).await() }

    actual fun resetAnalyticsData() {
        dev.gitlive.firebase.analytics.externals.resetAnalyticsData(js)
    }

    actual fun setDefaultEventParameters(parameters: Map<String, String>) {
        dev.gitlive.firebase.analytics.externals.setDefaultEventParameters(js, parameters)
    }

    actual fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>) {
        val consent = dev.gitlive.firebase.analytics.externals.ConsentSettings()
        consentSettings.forEach {
            when (it.key) {
                ConsentType.AD_PERSONALIZATION -> consent.ad_personalization = it.value.name
                ConsentType.AD_STORAGE -> consent.ad_storage = it.value.name
                ConsentType.AD_USER_DATA -> consent.ad_user_data = it.value.name
                ConsentType.ANALYTICS_STORAGE -> consent.analytics_storage = it.value.name
            }
        }
        dev.gitlive.firebase.analytics.externals.setConsent(js, consent)
    }

    actual enum class ConsentType {
        AD_PERSONALIZATION,
        AD_STORAGE,
        AD_USER_DATA,
        ANALYTICS_STORAGE
    }

    actual enum class ConsentStatus {
        GRANTED,
        DENIED
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