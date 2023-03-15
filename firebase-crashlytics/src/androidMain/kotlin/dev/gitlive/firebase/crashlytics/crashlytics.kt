package dev.gitlive.firebase.crashlytics

import com.google.firebase.FirebaseException
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp

actual val Firebase.crashlytics get() =
    FirebaseCrashlytics(com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance())

actual fun Firebase.crashlytics(app: FirebaseApp) =
    FirebaseCrashlytics(app.android.get(com.google.firebase.crashlytics.FirebaseCrashlytics::class.java))

actual class FirebaseCrashlytics internal constructor(val android: com.google.firebase.crashlytics.FirebaseCrashlytics){

    actual fun recordException(exception: Throwable) = android.recordException(exception)
    actual fun log(message: String) = android.log(message)
    actual fun setUserId(userId: String) = android.setUserId(userId)
    actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) = android.setCrashlyticsCollectionEnabled(enabled)
    actual fun sendUnsentReports() = android.sendUnsentReports()
    actual fun deleteUnsentReports() = android.deleteUnsentReports()
    actual fun didCrashOnPreviousExecution(): Boolean = android.didCrashOnPreviousExecution()
}

actual open class FirebaseCrashlyticsException(message: String) : FirebaseException(message)