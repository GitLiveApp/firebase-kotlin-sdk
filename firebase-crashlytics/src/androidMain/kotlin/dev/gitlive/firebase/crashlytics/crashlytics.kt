package dev.gitlive.firebase.crashlytics

import dev.gitlive.firebase.android.FirebaseException
import dev.gitlive.firebase.android.crashlytics.CustomKeysAndValues.Builder
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.android

public val FirebaseCrashlytics.android: dev.gitlive.firebase.android.crashlytics.FirebaseCrashlytics get() = dev.gitlive.firebase.android.crashlytics.FirebaseCrashlytics.getInstance()

public actual val Firebase.crashlytics: FirebaseCrashlytics get() =
    FirebaseCrashlytics(dev.gitlive.firebase.android.crashlytics.FirebaseCrashlytics.getInstance())

public actual fun Firebase.crashlytics(app: FirebaseApp): FirebaseCrashlytics = FirebaseCrashlytics(app.android.get(dev.gitlive.firebase.android.crashlytics.FirebaseCrashlytics::class.java))

public actual class FirebaseCrashlytics internal constructor(internal val android: dev.gitlive.firebase.android.crashlytics.FirebaseCrashlytics) {

    public actual fun recordException(exception: Throwable) {
        android.recordException(exception)
    }

    public actual fun recordException(exception: Throwable, customKeys: Map<String, Any>) {
        android.recordException(exception, customKeys.toCustomKeysAndValues())
    }

    public actual fun log(message: String) {
        android.log(message)
    }

    public actual fun setUserId(userId: String) {
        android.setUserId(userId)
    }

    public actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        android.setCrashlyticsCollectionEnabled(enabled)
    }

    public actual fun sendUnsentReports() {
        android.sendUnsentReports()
    }

    public actual fun deleteUnsentReports() {
        android.deleteUnsentReports()
    }

    public actual fun didCrashOnPreviousExecution(): Boolean = android.didCrashOnPreviousExecution()

    public actual fun setCustomKey(key: String, value: String) {
        android.setCustomKey(key, value)
    }

    public actual fun setCustomKey(key: String, value: Boolean) {
        android.setCustomKey(key, value)
    }

    public actual fun setCustomKey(key: String, value: Double) {
        android.setCustomKey(key, value)
    }

    public actual fun setCustomKey(key: String, value: Float) {
        android.setCustomKey(key, value)
    }

    public actual fun setCustomKey(key: String, value: Int) {
        android.setCustomKey(key, value)
    }

    public actual fun setCustomKey(key: String, value: Long) {
        android.setCustomKey(key, value)
    }

    public actual fun setCustomKeys(customKeys: Map<String, Any>) {
        android.setCustomKeys(customKeys.toCustomKeysAndValues())
    }

    private fun Map<String, Any>.toCustomKeysAndValues() = Builder().apply {
        forEach { (key, value) ->
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Double -> putDouble(key, value)
                is Float -> putFloat(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
            }
        }
    }.build()
}

public actual open class FirebaseCrashlyticsException(message: String) : FirebaseException(message)
