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

expect class FirebaseFunctions {
    fun httpsCallable(name: String, timeout: Long? = null): HttpsCallableReference
    fun useEmulator(host: String, port: Int)
}

@PublishedApi
internal expect class NativeHttpsCallableReference {
    suspend fun invoke(encodedData: Any): HttpsCallableResult
    suspend fun invoke(): HttpsCallableResult
}

class HttpsCallableReference internal constructor(
    @PublishedApi
    internal val native: NativeHttpsCallableReference,
) {
    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("invoke(data) { this.encodeDefaults = encodeDefaults }"))
    suspend inline operator fun <reified T> invoke(data: T, encodeDefaults: Boolean) = invoke(data) {
        this.encodeDefaults = encodeDefaults
    }
    suspend inline operator fun <reified T> invoke(data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): HttpsCallableResult = native.invoke(encodedData = encode(data, buildSettings)!!)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("invoke(strategy, data) { this.encodeDefaults = encodeDefaults }"))
    suspend operator fun <T> invoke(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean): HttpsCallableResult = invoke(strategy, data) {
        this.encodeDefaults = encodeDefaults
    }
    suspend inline operator fun <T> invoke(strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): HttpsCallableResult = invoke(encode(strategy, data, buildSettings)!!)
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

expect class FirebaseFunctionsException : FirebaseException

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
expect val FirebaseFunctionsException.code: FunctionsExceptionCode

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
expect val FirebaseFunctionsException.details: Any?

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
    UNAUTHENTICATED,
}
