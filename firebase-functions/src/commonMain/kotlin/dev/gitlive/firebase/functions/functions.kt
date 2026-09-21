/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.internal.encode
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import com.google.firebase.functions.FirebaseFunctions as CompatFirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference as CompatHttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult as CompatHttpsCallableResult
import com.google.firebase.functions.setTimeout as compatSetTimeout

/**
 * FirebaseFunctions lets you call Cloud Functions for Firebase.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.functions.FirebaseFunctions] this wraps.
 */
public class FirebaseFunctions internal constructor(public val compat: CompatFirebaseFunctions) {
    /** Returns a reference to the callable HTTPS trigger with the given name. */
    public fun httpsCallable(name: String, timeout: Duration? = null): HttpsCallableReference = HttpsCallableReference(compat.getHttpsCallable(name).apply { timeout?.let { compatSetTimeout(it) } })

    /**
     * Modifies this FirebaseFunctions instance to communicate with the Cloud Functions emulator.
     *
     * Note: Call this method before using the instance to do any functions operations.
     *
     * @param host the emulator host (for example, 10.0.2.2)
     * @param port the emulator port (for example, 5001)
     */
    public fun useEmulator(host: String, port: Int) {
        compat.useEmulator(host, port)
    }

    override fun equals(other: Any?): Boolean = other is FirebaseFunctions && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = "FirebaseFunctions($compat)"
}

@Deprecated("Replaced with Kotlin Duration", replaceWith = ReplaceWith("httpsCallable(name, timeout.milliseconds)"))
public fun FirebaseFunctions.httpsCallable(name: String, timeout: Long): HttpsCallableReference = httpsCallable(name, timeout.milliseconds)

/**
 * A reference to a particular Callable HTTPS trigger in Cloud Functions.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.functions.HttpsCallableReference] this wraps.
 */
public class HttpsCallableReference internal constructor(public val compat: CompatHttpsCallableReference) {
    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("invoke(data) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline operator fun <reified T> invoke(data: T, encodeDefaults: Boolean): HttpsCallableResult = invoke(data) {
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
    public suspend inline operator fun <reified T> invoke(data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): HttpsCallableResult = call(encode(data, buildSettings)!!)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("invoke(strategy, data) { this.encodeDefaults = encodeDefaults }"))
    public suspend operator fun <T> invoke(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean): HttpsCallableResult = invoke(strategy, data) {
        this.encodeDefaults = encodeDefaults
    }

    public suspend inline operator fun <T> invoke(strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): HttpsCallableResult = call(encode(strategy, data, buildSettings)!!)

    /**
     * Executes this HTTPS endpoint asynchronously without arguments.
     *
     * The request to the Cloud Functions backend made by this method automatically includes a
     * Firebase Instance ID token to identify the app instance. If a user is logged in with Firebase
     * Auth, an auth token for the user will also be automatically included.
     *
     * @return A [HttpsCallableResult] that will contain the result.
     */
    public suspend operator fun invoke(): HttpsCallableResult = HttpsCallableResult(compat.call().await())

    @PublishedApi
    internal suspend fun call(encodedData: Any): HttpsCallableResult = HttpsCallableResult(compat.call(encodedData).await())
}

/**
 * The result of a call through an [HttpsCallableReference].
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.functions.HttpsCallableResult] this wraps.
 */
public class HttpsCallableResult(public val compat: CompatHttpsCallableResult) {

    public inline fun <reified T> data(): T = decode<T>(value = compat.data)

    public inline fun <T> data(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(strategy, compat.data, buildSettings)
}

/**
 * Exception that gets thrown when an operation on Firebase Functions fails; its `code` is a [FunctionsExceptionCode]
 * and its `details` the error details the function sent.
 */
public typealias FirebaseFunctionsException = com.google.firebase.functions.FirebaseFunctionsException

/**
 * The set of error status codes that can be returned from a Callable HTTPS tigger. These are the
 * canonical error codes for Google APIs, as documented here:
 * https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto#L26
 */
public typealias FunctionsExceptionCode = com.google.firebase.functions.FirebaseFunctionsException.Code
