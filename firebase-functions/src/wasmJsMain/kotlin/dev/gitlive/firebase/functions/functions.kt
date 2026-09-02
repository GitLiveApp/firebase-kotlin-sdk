/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package dev.gitlive.firebase.functions

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.externals.awaitValue
import dev.gitlive.firebase.externals.errorCode
import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.json
import dev.gitlive.firebase.externals.stringifyThrownValue
import dev.gitlive.firebase.externals.toJs
import dev.gitlive.firebase.externals.toKotlin
import dev.gitlive.firebase.functions.externals.Functions
import dev.gitlive.firebase.functions.externals.HttpsCallable
import dev.gitlive.firebase.functions.externals.connectFunctionsEmulator
import dev.gitlive.firebase.functions.externals.getFunctions
import dev.gitlive.firebase.functions.externals.httpsCallable
import dev.gitlive.firebase.functions.externals.invoke
import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.js
import kotlinx.serialization.DeserializationStrategy
import kotlin.js.JsException
import kotlin.time.Duration
import kotlin.time.DurationUnit
import dev.gitlive.firebase.functions.externals.HttpsCallableResult as JsHttpsCallableResult
import dev.gitlive.firebase.functions.js as publicJs

public actual val Firebase.functions: FirebaseFunctions
    get() = rethrow { FirebaseFunctions(getFunctions()) }

public actual fun Firebase.functions(region: String): FirebaseFunctions = rethrow { FirebaseFunctions(getFunctions(regionOrCustomDomain = region)) }

public actual fun Firebase.functions(app: FirebaseApp): FirebaseFunctions = rethrow { FirebaseFunctions(getFunctions(app.js)) }

public actual fun Firebase.functions(app: FirebaseApp, region: String): FirebaseFunctions = rethrow { FirebaseFunctions(getFunctions(app.js, region)) }

public val FirebaseFunctions.js: Functions get() = js

public actual class FirebaseFunctions internal constructor(internal val js: Functions) {
    public actual fun httpsCallable(name: String, timeout: Duration?): HttpsCallableReference = rethrow { HttpsCallableReference(httpsCallable(js, name, timeout?.let { json("timeout" to timeout.toDouble(DurationUnit.MILLISECONDS)) }).native) }

    public actual fun useEmulator(host: String, port: Int) {
        connectFunctionsEmulator(js, host, port)
    }
}

@PublishedApi
internal actual data class NativeHttpsCallableReference(val js: HttpsCallable) {
    actual suspend fun invoke(encodedData: Any): HttpsCallableResult = rethrow {
        HttpsCallableResult(js.invoke(encodedData.toJs()).awaitValue())
    }
    actual suspend fun invoke(): HttpsCallableResult = rethrow { HttpsCallableResult(js.invoke().awaitValue()) }
}

@PublishedApi
internal val HttpsCallable.native: NativeHttpsCallableReference get() = NativeHttpsCallableReference(this)

public val HttpsCallableReference.js: HttpsCallable get() = native.js

public val HttpsCallableResult.js: JsHttpsCallableResult get() = js

public actual class HttpsCallableResult(internal val js: JsHttpsCallableResult) {

    public actual inline fun <reified T> data(): T = rethrow { decode<T>(value = publicJs.data.toKotlin()) }

    public actual inline fun <T> data(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit): T = rethrow { decode(strategy, publicJs.data.toKotlin(), buildSettings) }
}

public actual class FirebaseFunctionsException(cause: Throwable, public val code: FunctionsExceptionCode, public val details: Any?) : FirebaseException(cause.message, cause)

public actual val FirebaseFunctionsException.code: FunctionsExceptionCode get() = code

public actual val FirebaseFunctionsException.details: Any? get() = details

public actual enum class FunctionsExceptionCode {
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

@PublishedApi
internal inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.functions.rethrow { function() }

@PublishedApi
internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: JsException) {
        throw errorToException(e)
    }
}

@PublishedApi
internal fun errorToException(cause: JsException): FirebaseFunctionsException {
    val details = cause.thrownValue?.let { jsGet(it, "details") }
    return (cause.errorCode() ?: cause.message ?: "")
        .lowercase()
        .let {
            when {
                "cancelled" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.CANCELLED, details)
                "invalid-argument" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.INVALID_ARGUMENT, details)
                "deadline-exceeded" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.DEADLINE_EXCEEDED, details)
                "not-found" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.NOT_FOUND, details)
                "already-exists" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.ALREADY_EXISTS, details)
                "permission-denied" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.PERMISSION_DENIED, details)
                "resource-exhausted" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.RESOURCE_EXHAUSTED, details)
                "failed-precondition" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.FAILED_PRECONDITION, details)
                "aborted" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.ABORTED, details)
                "out-of-range" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.OUT_OF_RANGE, details)
                "unimplemented" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.UNIMPLEMENTED, details)
                "internal" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.INTERNAL, details)
                "unavailable" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.UNAVAILABLE, details)
                "data-loss" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.DATA_LOSS, details)
                "unauthenticated" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.UNAUTHENTICATED, details)
                "unknown" in it -> FirebaseFunctionsException(cause, FunctionsExceptionCode.UNKNOWN, details)
                else -> {
                    println("Unknown error code in ${cause.stringifyThrownValue()}")
                    FirebaseFunctionsException(cause, FunctionsExceptionCode.UNKNOWN, details)
                }
            }
        }
}
