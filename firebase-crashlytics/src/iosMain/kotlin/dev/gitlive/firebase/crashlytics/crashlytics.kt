package dev.gitlive.firebase.crashlytics

import cocoapods.FirebaseCrashlytics.FIRCrashlytics
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException

actual val Firebase.crashlytics get() =
    FirebaseCrashlytics(FIRCrashlytics.crashlytics())

actual fun Firebase.crashlytics(app: FirebaseApp) =
    FirebaseCrashlytics(FIRCrashlytics.crashlytics())

actual class FirebaseCrashlytics internal constructor(val ios: FIRCrashlytics) {

    actual fun recordException(exception: Throwable) {
        ios.recordError(exception.asNSError())
    }
}

actual open class FirebaseCrashlyticsException internal constructor(message: String) : FirebaseException(message)

private fun Throwable.asNSError(): NSError {
    val userInfo = mutableMapOf<Any?, Any>()
    userInfo["KotlinException"] = this
    val message = message
    if (message != null) {
        userInfo[NSLocalizedDescriptionKey] = message
    }
    return NSError.errorWithDomain("KotlinException", 0, userInfo)
}