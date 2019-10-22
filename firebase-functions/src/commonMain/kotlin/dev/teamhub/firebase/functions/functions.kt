package dev.teamhub.firebase.functions

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException

expect class FirebaseFunctions {
    fun getHttpsCallable(name: String, timeout: Long? = null): HttpsCallableReference
}

expect class HttpsCallableReference {
    suspend fun call(data: Any?): HttpsCallableResult
    suspend fun call(): HttpsCallableResult
}

expect class HttpsCallableResult {
    val data: Any?
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

