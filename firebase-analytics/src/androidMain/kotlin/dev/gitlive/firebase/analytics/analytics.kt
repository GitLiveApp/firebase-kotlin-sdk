@file:JvmName("analyticsAndroid")

package dev.gitlive.firebase.analytics

import android.os.Bundle
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.android
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration

public actual val Firebase.analytics: FirebaseAnalytics
    get() = FirebaseAnalytics(
        dev.gitlive.firebase.android.analytics.FirebaseAnalytics.getInstance(dev.gitlive.firebase.android.FirebaseApp.getInstance().applicationContext),
    )

public actual fun Firebase.analytics(app: FirebaseApp): FirebaseAnalytics = FirebaseAnalytics(dev.gitlive.firebase.android.analytics.FirebaseAnalytics.getInstance(app.android.applicationContext))

public val FirebaseAnalytics.android: dev.gitlive.firebase.android.analytics.FirebaseAnalytics get() = android

public actual class FirebaseAnalytics(internal val android: dev.gitlive.firebase.android.analytics.FirebaseAnalytics) {
    public actual fun logEvent(name: String, parameters: Map<String, Any>?) {
        android.logEvent(name, parameters?.toBundle())
    }
    public actual fun setUserProperty(name: String, value: String) {
        android.setUserProperty(name, value)
    }
    public actual fun setUserId(id: String?) {
        android.setUserId(id)
    }
    public actual fun resetAnalyticsData() {
        android.resetAnalyticsData()
    }
    public actual fun setDefaultEventParameters(parameters: Map<String, String>) {
        android.setDefaultEventParameters(parameters.toBundle())
    }

    public actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        android.setAnalyticsCollectionEnabled(enabled)
    }

    public actual fun setSessionTimeoutInterval(sessionTimeoutInterval: Duration) {
        android.setSessionTimeoutDuration(sessionTimeoutInterval.inWholeMilliseconds)
    }

    public actual suspend fun getSessionId(): Long? = android.sessionId.await()

    public actual fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>) {
        android.setConsent(
            consentSettings.entries.associate {
                val type = when (it.key) {
                    ConsentType.AD_PERSONALIZATION -> dev.gitlive.firebase.android.analytics.FirebaseAnalytics.ConsentType.AD_PERSONALIZATION
                    ConsentType.AD_STORAGE -> dev.gitlive.firebase.android.analytics.FirebaseAnalytics.ConsentType.AD_STORAGE
                    ConsentType.AD_USER_DATA -> dev.gitlive.firebase.android.analytics.FirebaseAnalytics.ConsentType.AD_USER_DATA
                    ConsentType.ANALYTICS_STORAGE -> dev.gitlive.firebase.android.analytics.FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE
                }
                val status = when (it.value) {
                    ConsentStatus.GRANTED -> dev.gitlive.firebase.android.analytics.FirebaseAnalytics.ConsentStatus.GRANTED
                    ConsentStatus.DENIED -> dev.gitlive.firebase.android.analytics.FirebaseAnalytics.ConsentStatus.DENIED
                }
                type to status
            },
        )
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

public actual class FirebaseAnalyticsException(message: String) : Exception(message)

private fun Map<String, Any>.toBundle() = Bundle().apply {
    forEach { (key, value) ->
        when (value::class) {
            String::class -> putString(key, value as String)
            Int::class -> putInt(key, value as Int)
            Long::class -> putLong(key, value as Long)
            Double::class -> putDouble(key, value as Double)
            Boolean::class -> putBoolean(key, value as Boolean)
        }
    }
}
