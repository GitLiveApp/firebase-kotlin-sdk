/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("DEPRECATION")

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

/** Message of the deprecated `dev.gitlive` members that only delegate to the `com.google.firebase` layer. */
internal const val DELEGATES_TO_ANDROID_SDK_API =
    "Only delegates to the com.google.firebase layer, which common code can use directly; see the ReplaceWith"

// The serialization API, the part of this wrapper that adds to the Android SDK shape, as extensions of the
// com.google.firebase.functions classes.

/**
 * Executes this callable HTTPS trigger with [data], serialized with kotlinx.serialization, and suspends until the
 * result arrives. A failed call throws a [FirebaseFunctionsException]. See [CompatHttpsCallableReference.call].
 */
public suspend inline operator fun <reified T> CompatHttpsCallableReference.invoke(data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): CompatHttpsCallableResult = call(encode(data, buildSettings)!!).await()

/** Executes this callable HTTPS trigger with [data] serialized by [strategy]; see [invoke]. */
public suspend inline operator fun <T> CompatHttpsCallableReference.invoke(strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): CompatHttpsCallableResult = call(encode(strategy, data, buildSettings)!!).await()

/** Executes this callable HTTPS trigger without arguments and suspends until the result arrives; see [invoke]. */
public suspend operator fun CompatHttpsCallableReference.invoke(): CompatHttpsCallableResult = call().await()

/** The [CompatHttpsCallableResult.data] deserialized as [T] with kotlinx.serialization. */
public inline fun <reified T> CompatHttpsCallableResult.data(): T = decode<T>(value = data)

/** The [CompatHttpsCallableResult.data] deserialized with [strategy]. */
public inline fun <T> CompatHttpsCallableResult.data(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(strategy, data, buildSettings)

/**
 * FirebaseFunctions lets you call Cloud Functions for Firebase.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.functions.FirebaseFunctions] this wraps.
 */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.functions.FirebaseFunctions"))
public class FirebaseFunctions internal constructor(public val compat: CompatFirebaseFunctions) {
    /** Returns a reference to the callable HTTPS trigger with the given name. */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.getHttpsCallable(name).apply { timeout?.let { setTimeout(it) } }", "com.google.firebase.functions.setTimeout"))
    public fun httpsCallable(name: String, timeout: Duration? = null): HttpsCallableReference = HttpsCallableReference(compat.getHttpsCallable(name).apply { timeout?.let { compatSetTimeout(it) } })

    /**
     * Modifies this FirebaseFunctions instance to communicate with the Cloud Functions emulator.
     *
     * Note: Call this method before using the instance to do any functions operations.
     *
     * @param host the emulator host (for example, 10.0.2.2)
     * @param port the emulator port (for example, 5001)
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.useEmulator(host, port)"))
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
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.functions.HttpsCallableReference"))
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
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.invoke(data, buildSettings)", "dev.gitlive.firebase.functions.invoke"))
    public suspend inline operator fun <reified T> invoke(data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): HttpsCallableResult = HttpsCallableResult(compat.invoke(data, buildSettings))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("invoke(strategy, data) { this.encodeDefaults = encodeDefaults }"))
    public suspend operator fun <T> invoke(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean): HttpsCallableResult = invoke(strategy, data) {
        this.encodeDefaults = encodeDefaults
    }

    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.invoke(strategy, data, buildSettings)", "dev.gitlive.firebase.functions.invoke"))
    public suspend inline operator fun <T> invoke(strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): HttpsCallableResult = HttpsCallableResult(compat.invoke(strategy, data, buildSettings))

    /**
     * Executes this HTTPS endpoint asynchronously without arguments.
     *
     * The request to the Cloud Functions backend made by this method automatically includes a
     * Firebase Instance ID token to identify the app instance. If a user is logged in with Firebase
     * Auth, an auth token for the user will also be automatically included.
     *
     * @return A [HttpsCallableResult] that will contain the result.
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.invoke()", "dev.gitlive.firebase.functions.invoke"))
    public suspend operator fun invoke(): HttpsCallableResult = HttpsCallableResult(compat.invoke())
}

/**
 * The result of a call through an [HttpsCallableReference].
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.functions.HttpsCallableResult] this wraps.
 */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.functions.HttpsCallableResult"))
public class HttpsCallableResult(public val compat: CompatHttpsCallableResult) {

    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.data<T>()", "dev.gitlive.firebase.functions.data"))
    public inline fun <reified T> data(): T = compat.data<T>()

    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.data(strategy, buildSettings)", "dev.gitlive.firebase.functions.data"))
    public inline fun <T> data(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = compat.data(strategy, buildSettings)
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
