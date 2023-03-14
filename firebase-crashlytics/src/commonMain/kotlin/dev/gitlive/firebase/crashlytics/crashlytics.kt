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
}

expect open class FirebaseCrashlyticsException : FirebaseException