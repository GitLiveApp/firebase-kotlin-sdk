package dev.gitlive.firebase.crashlytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException

/** Returns the [FirebaseCrashlytics] instance of the default [FirebaseApp]. */
actual val Firebase.crashlytics: FirebaseCrashlytics
    get() = TODO("Not yet implemented")

/** Returns the [FirebaseCrashlytics] instance of a given [FirebaseApp]. */
actual fun Firebase.crashlytics(app: FirebaseApp): FirebaseCrashlytics {
    TODO("Not yet implemented")
}

actual class FirebaseCrashlytics {
    actual fun recordException(exception: Throwable) {
    }

    actual fun log(message: String) {
    }

    actual fun setUserId(userId: String) {
    }

    actual fun setCustomKey(key: String, value: String) {
    }

    actual fun setCustomKey(key: String, value: Boolean) {
    }

    actual fun setCustomKey(key: String, value: Double) {
    }

    actual fun setCustomKey(key: String, value: Float) {
    }

    actual fun setCustomKey(key: String, value: Int) {
    }

    actual fun setCustomKey(key: String, value: Long) {
    }

    actual fun setCustomKeys(customKeys: Map<String, Any>) {
    }

    actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
    }

    actual fun didCrashOnPreviousExecution(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun sendUnsentReports() {
    }

    actual fun deleteUnsentReports() {
    }
}

actual open class FirebaseCrashlyticsException internal constructor(message: String) : FirebaseException(message)