/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.internal.encode
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

/** FirebaseFunctions lets you call Cloud Functions for Firebase. */
expect class FirebaseFunctions {
    /** Returns a reference to the callable HTTPS trigger with the given name. */
    fun httpsCallable(name: String, timeout: Long? = null): HttpsCallableReference

    /**
     * Modifies this FirebaseFunctions instance to communicate with the Cloud Functions emulator.
     *
     * Note: Call this method before using the instance to do any functions operations.
     *
     * @param host the emulator host (for example, 10.0.2.2)
     * @param port the emulator port (for example, 5001)
     */
    fun useEmulator(host: String, port: Int)
}

@PublishedApi
internal expect class NativeHttpsCallableReference {
    suspend fun invoke(encodedData: Any): HttpsCallableResult
    suspend fun invoke(): HttpsCallableResult
}

/** A reference to a particular Callable HTTPS trigger in Cloud Functions. */
class HttpsCallableReference internal constructor(
    @PublishedApi
    internal val native: NativeHttpsCallableReference
) {
    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("invoke(data) { this.encodeDefaults = encodeDefaults }"))
    suspend inline operator fun <reified T> invoke(data: T, encodeDefaults: Boolean) = invoke(data) {
        this.encodeDefaults = encodeDefaults
    }

    /**
     * Executes this Callable HTTPS trigger asynchronously.
     *
     * If the returned task fails, the Exception will be one of the following types:
     * - [FirebaseFunctionsException] - if the request connected, but the function returned
     *       an error.
     *
     * The request to the Cloud Functions backend made by this method automatically includes a
     * Firebase Instance ID token to identify the app instance. If a user is logged in with Firebase
     * Auth, an auth token for the user will also be automatically included.
     *
     * @param data Parameters to pass to the trigger.
     * @return A Task that will be completed when the HTTPS request has completed.
     * @see FirebaseFunctionsException
     */
    suspend inline operator fun <reified T> invoke(data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): HttpsCallableResult = native.invoke(encodedData = encode(data, buildSettings)!!)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("invoke(strategy, data) { this.encodeDefaults = encodeDefaults }"))
    suspend operator fun <T> invoke(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean): HttpsCallableResult = invoke(strategy, data) {
        this.encodeDefaults = encodeDefaults
    }

    suspend inline operator fun <T> invoke(strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): HttpsCallableResult = invoke(encode(strategy, data, buildSettings)!!)

    /**
     * Executes this HTTPS endpoint asynchronously without arguments.
     *
     * The request to the Cloud Functions backend made by this method automatically includes a
     * Firebase Instance ID token to identify the app instance. If a user is logged in with Firebase
     * Auth, an auth token for the user will also be automatically included.
     *
     * @return A [HttpsCallableResult] that will contain the result.
     */
    suspend operator fun invoke(): HttpsCallableResult = native.invoke()
}

expect class HttpsCallableResult {
    inline fun <reified T> data(): T
    inline fun <T> data(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T
}

/** Returns the [FirebaseFunctions] instance of the default [FirebaseApp]. */
expect val Firebase.functions: FirebaseFunctions

/** Returns the [FirebaseFunctions] instance of a given [region]. */
expect fun Firebase.functions(region: String): FirebaseFunctions

/** Returns the [FirebaseFunctions] instance of a given [FirebaseApp]. */
expect fun Firebase.functions(app: FirebaseApp): FirebaseFunctions

/** Returns the [FirebaseFunctions] instance of a given [FirebaseApp] and [region]. */
expect fun Firebase.functions(app: FirebaseApp, region: String): FirebaseFunctions

/**
 * Exception that gets thrown when an operation on Firebase Functions fails.
 */
expect class FirebaseFunctionsException: FirebaseException

/**
 * Returns the error code for this exception.
 *
 * @return [code] [FunctionsExceptionCode] that caused the exception.
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
expect val FirebaseFunctionsException.code: FunctionsExceptionCode

/**
 * Returns the message for this exception.
 *
 * @return [details] message for this exception.
 */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
expect val FirebaseFunctionsException.details: Any?

/**
 * The set of error status codes that can be returned from a Callable HTTPS tigger. These are the
 * canonical error codes for Google APIs, as documented here:
 * https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto#L26
 */
expect enum class FunctionsExceptionCode {
    OK,
    CANCELLED,
    UNKNOWN,
    INVALID_ARGUMENT,
    DEADLINE_EXCEEDED,
    NOT_FOUND,
    ALREADY_EXISTS,
    PERMISSION_DENIED,
    RESOURCE_EXHAUSTED,
    FAILED_PRECONDITION,
    ABORTED,
    OUT_OF_RANGE,
    UNIMPLEMENTED,
    INTERNAL,
    UNAVAILABLE,
    DATA_LOSS,
    UNAUTHENTICATED
}
