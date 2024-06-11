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
    actual fun log(message: String) {
        ios.log(message)
    }
    actual fun setUserId(userId: String) {
        ios.setUserID(userId)
    }
    actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        ios.setCrashlyticsCollectionEnabled(enabled)
    }
    actual fun sendUnsentReports() {
        ios.sendUnsentReports()
    }
    actual fun deleteUnsentReports() {
        ios.deleteUnsentReports()
    }
    actual fun didCrashOnPreviousExecution(): Boolean = ios.didCrashDuringPreviousExecution()
    actual fun setCustomKey(key: String, value: String) {
        ios.setCustomValue(key, value)
    }
    actual fun setCustomKey(key: String, value: Boolean) {
        ios.setCustomValue(key, value.toString())
    }
    actual fun setCustomKey(key: String, value: Double) {
        ios.setCustomValue(key, value.toString())
    }
    actual fun setCustomKey(key: String, value: Float) {
        ios.setCustomValue(key, value.toString())
    }
    actual fun setCustomKey(key: String, value: Int) {
        ios.setCustomValue(key, value.toString())
    }
    actual fun setCustomKey(key: String, value: Long) {
        ios.setCustomValue(key, value.toString())
    }

    @Suppress("UNCHECKED_CAST")
    actual fun setCustomKeys(customKeys: Map<String, Any>) {
        ios.setCustomKeysAndValues(customKeys as Map<Any?, *>)
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
    return NSError.errorWithDomain(this::class.qualifiedName, 0, userInfo)
}
