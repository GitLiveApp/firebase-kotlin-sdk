package dev.teamhub.firebase

import dev.teamhub.firebase.common.firebase
import kotlin.js.json

actual val Firebase.app: FirebaseApp
    get() = FirebaseApp(firebase.app())

actual fun Firebase.app(name: String): FirebaseApp =
    FirebaseApp(firebase.app(name))

actual fun Firebase.initialize(context: Any?): FirebaseApp? =
    throw UnsupportedOperationException("Cannot initialize firebase without options in JS")

actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp =
    FirebaseApp(firebase.initializeApp(options.toJson(), name))

actual fun Firebase.initialize(context: Any?, options: FirebaseOptions) =
    FirebaseApp(firebase.initializeApp(options.toJson()))

actual class FirebaseApp internal constructor(val js: firebase.App) {
    actual val name: String
        get() = js.name
    actual val options: FirebaseOptions
        get() = js.options.run {
            FirebaseOptions(applicationId, apiKey, databaseUrl, gaTrackingId, storageBucket, projectId)
        }
}

actual fun Firebase.apps(context: Any?) = firebase.apps.map { FirebaseApp(it) }

private fun FirebaseOptions.toJson() = json(
    "apiKey" to apiKey,
    "applicationId" to applicationId,
    "databaseURL" to databaseUrl,
    "storageBucket" to storageBucket,
    "projectId" to projectId,
    "gaTrackingId" to gaTrackingId
)

actual open class FirebaseException(code: String?, message: String?) : Exception("$code: $message")
actual open class FirebaseNetworkException(code: String?, message: String?) : FirebaseException(code, message)
actual open class FirebaseTooManyRequestsException(code: String?, message: String?) : FirebaseException(code, message)
actual open class FirebaseApiNotAvailableException(code: String?, message: String?) : FirebaseException(code, message)
