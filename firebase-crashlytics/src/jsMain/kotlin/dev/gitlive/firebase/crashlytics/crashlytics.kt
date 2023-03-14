package dev.gitlive.firebase.crashlytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.firebase

actual val Firebase.crashlytics: FirebaseCrashlytics
    get() = rethrow {
        FirebaseCrashlytics(firebase.crashlytics())
    }

actual fun Firebase.crashlytics(app: FirebaseApp): FirebaseCrashlytics = rethrow {
    FirebaseCrashlytics(firebase.crashlytics(app.js))
}

actual class FirebaseCrashlytics internal constructor(val js: firebase.crashlytics.Crashlytics) {
    actual fun recordException(exception: Throwable) = rethrow {
        js.recordException(exception)
    }
}

actual open class FirebaseCrashlyticsException(code: String, cause: Throwable) : FirebaseException(code, cause)

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
                FirebaseCrashlyticsException(code, error)
            }
        }
    }
