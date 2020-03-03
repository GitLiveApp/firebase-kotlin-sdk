package dev.teamhub.firebase

actual class FirebaseApp {
    actual val name: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual val options: FirebaseOptions
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}

/** Returns the default firebase app instance. */
actual val Firebase.app: FirebaseApp
    get() = kotlin.TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

actual fun Firebase.app(name: String): FirebaseApp = kotlin.TODO("not implemented")

actual fun Firebase.apps(context: Any?): List<FirebaseApp> = kotlin.TODO("not implemented")

actual fun Firebase.initialize(context: Any?): FirebaseApp? = kotlin.TODO("not implemented")

/** Initializes and returns a FirebaseApp. */
actual fun Firebase.initialize(context: Any?, options: FirebaseOptions): FirebaseApp = kotlin.TODO("not implemented")

/** Initializes and returns a FirebaseApp. */
actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = kotlin.TODO("not implemented")

actual open class FirebaseException : Exception()

actual class FirebaseNetworkException : FirebaseException()

actual open class FirebaseTooManyRequestsException : FirebaseException()

actual open class FirebaseApiNotAvailableException : FirebaseException()