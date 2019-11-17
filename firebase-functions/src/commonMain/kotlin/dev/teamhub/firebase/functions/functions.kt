package dev.teamhub.firebase.functions

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerializationStrategy

expect class FirebaseFunctions {
    fun httpsCallable(name: String, timeout: Long? = null): HttpsCallableReference
}

expect class HttpsCallableReference {
    @ImplicitReflectionSerializer
    suspend inline fun <reified T: Any> call(data: T): HttpsCallableResult
    suspend inline fun <reified T> call(data: T, strategy: SerializationStrategy<T>): HttpsCallableResult
    suspend fun call(): HttpsCallableResult
}

expect class HttpsCallableResult {
    @ImplicitReflectionSerializer
    inline fun <reified T: Any> data(): T
    inline fun <reified T> data(strategy: DeserializationStrategy<T>): T
}

/** Returns the [FirebaseFunctions] instance of the default [FirebaseApp]. */
expect val Firebase.functions: FirebaseFunctions

/** Returns the [FirebaseFunctions] instance of a given [region]. */
expect fun Firebase.functions(region: String): FirebaseFunctions

/** Returns the [FirebaseFunctions] instance of a given [FirebaseApp]. */
expect fun Firebase.functions(app: FirebaseApp): FirebaseFunctions

/** Returns the [FirebaseFunctions] instance of a given [FirebaseApp] and [region]. */
expect fun Firebase.functions(app: FirebaseApp, region: String): FirebaseFunctions

expect class FirebaseFunctionsException: FirebaseException

