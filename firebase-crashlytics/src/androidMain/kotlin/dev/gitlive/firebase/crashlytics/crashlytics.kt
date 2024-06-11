package dev.gitlive.firebase.crashlytics

import com.google.firebase.FirebaseException
import com.google.firebase.crashlytics.CustomKeysAndValues.Builder
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp

actual val Firebase.crashlytics get() =
    FirebaseCrashlytics(com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance())

actual fun Firebase.crashlytics(app: FirebaseApp) =
    FirebaseCrashlytics(app.android.get(com.google.firebase.crashlytics.FirebaseCrashlytics::class.java))

actual class FirebaseCrashlytics internal constructor(val android: com.google.firebase.crashlytics.FirebaseCrashlytics) {

    actual fun recordException(exception: Throwable) = android.recordException(exception)
    actual fun log(message: String) = android.log(message)
    actual fun setUserId(userId: String) = android.setUserId(userId)
    actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) = android.setCrashlyticsCollectionEnabled(enabled)
    actual fun sendUnsentReports() = android.sendUnsentReports()
    actual fun deleteUnsentReports() = android.deleteUnsentReports()
    actual fun didCrashOnPreviousExecution(): Boolean = android.didCrashOnPreviousExecution()
    actual fun setCustomKey(key: String, value: String) = android.setCustomKey(key, value)
    actual fun setCustomKey(key: String, value: Boolean) = android.setCustomKey(key, value)
    actual fun setCustomKey(key: String, value: Double) = android.setCustomKey(key, value)
    actual fun setCustomKey(key: String, value: Float) = android.setCustomKey(key, value)
    actual fun setCustomKey(key: String, value: Int) = android.setCustomKey(key, value)
    actual fun setCustomKey(key: String, value: Long) = android.setCustomKey(key, value)
    actual fun setCustomKeys(customKeys: Map<String, Any>) =
        android.setCustomKeys(
            Builder().apply {
                customKeys.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Boolean -> putBoolean(key, value)
                        is Double -> putDouble(key, value)
                        is Float -> putFloat(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                    }
                }
            }.build(),
        )
}

actual open class FirebaseCrashlyticsException(message: String) : FirebaseException(message)
