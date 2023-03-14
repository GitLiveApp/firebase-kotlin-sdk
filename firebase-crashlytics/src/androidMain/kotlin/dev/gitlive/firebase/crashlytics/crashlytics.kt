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
}

actual open class FirebaseCrashlyticsException(message: String) : FirebaseException(message)