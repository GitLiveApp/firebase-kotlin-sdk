package dev.teamhub.firebase.functions

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException

expect class FirebaseFunctions {
    fun httpsCallable(name: String, timeout: Long? = null): HttpsCallableReference
}

expect class HttpsCallableReference {
    suspend inline fun <reified T: Any> call(data: T): HttpsCallableResult
    suspend fun call(): HttpsCallableResult
}

expect class HttpsCallableResult {
    /**
     * Returns the contents of the document converted to a POJO or null if the document doesn't exist.
     *
     * @param T The type of the object to create.
     * @return The contents of the document in an object of type T or null if the document doesn't
     *     exist.
     */
    inline fun <reified T: Any> get(): T?
    inline fun <reified T: Any> getList(): List<T>?
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

