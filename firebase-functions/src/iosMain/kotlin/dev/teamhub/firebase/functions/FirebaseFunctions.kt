package dev.teamhub.firebase.functions

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.ImplicitReflectionSerializer

actual class FirebaseFunctions {
    actual fun httpsCallable(
        name: String,
        timeout: Long?
    ): HttpsCallableReference {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual class HttpsCallableReference {
    @ImplicitReflectionSerializer
    actual suspend inline fun <reified T> call(data: T): HttpsCallableResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual suspend inline fun <reified T> call(
        strategy: SerializationStrategy<T>,
        data: T
    ): HttpsCallableResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual suspend fun call(): HttpsCallableResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

actual class HttpsCallableResult {
    @ImplicitReflectionSerializer
    actual inline fun <reified T> data(): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual inline fun <reified T> data(strategy: DeserializationStrategy<T>): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

/** Returns the [FirebaseFunctions] instance of the default [FirebaseApp]. */
actual val Firebase.functions: FirebaseFunctions
    get() = kotlin.TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

/** Returns the [FirebaseFunctions] instance of a given [region]. */
actual fun Firebase.functions(region: String): FirebaseFunctions {
    kotlin.TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

/** Returns the [FirebaseFunctions] instance of a given [FirebaseApp]. */
actual fun Firebase.functions(app: FirebaseApp): FirebaseFunctions {
    kotlin.TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

/** Returns the [FirebaseFunctions] instance of a given [FirebaseApp] and [region]. */
actual fun Firebase.functions(
    app: FirebaseApp,
    region: String
): FirebaseFunctions {
    kotlin.TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual class FirebaseFunctionsException : FirebaseException()
