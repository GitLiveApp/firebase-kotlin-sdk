package dev.gitlive.firebase.crashlytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException

/** Returns the [FirebaseCrashlytics] instance of the default [FirebaseApp]. */
expect val Firebase.crashlytics: FirebaseCrashlytics

/** Returns the [FirebaseCrashlytics] instance of a given [FirebaseApp]. */
expect fun Firebase.crashlytics(app: FirebaseApp): FirebaseCrashlytics

expect class FirebaseCrashlytics {
    fun recordException(exception: Throwable)
    fun log(message: String)
    fun setUserId(userId: String)
    fun setCustomKey(key: String, value: String)
    fun setCustomKey(key: String, value: Boolean)
    fun setCustomKey(key: String, value: Double)
    fun setCustomKey(key: String, value: Float)
    fun setCustomKey(key: String, value: Int)
    fun setCustomKey(key: String, value: Long)
    fun setCustomKeys(customKeys: Map<String, Any>)
    fun setCrashlyticsCollectionEnabled(enabled: Boolean)
    fun didCrashOnPreviousExecution(): Boolean
    fun sendUnsentReports()
    fun deleteUnsentReports()
}

expect open class FirebaseCrashlyticsException : FirebaseException
