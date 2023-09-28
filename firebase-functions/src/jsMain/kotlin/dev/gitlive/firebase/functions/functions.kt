/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import dev.gitlive.firebase.*
import dev.gitlive.firebase.functions.externals.*
import kotlinx.coroutines.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlin.js.json
import dev.gitlive.firebase.functions.externals.HttpsCallableResult as JsHttpsCallableResult

actual val Firebase.functions: FirebaseFunctions
    get() = rethrow { FirebaseFunctions(getFunctions()) }

actual fun Firebase.functions(region: String) =
    rethrow { FirebaseFunctions(getFunctions(regionOrCustomDomain = region)) }

actual fun Firebase.functions(app: FirebaseApp) =
    rethrow { FirebaseFunctions(getFunctions(app.js)) }

actual fun Firebase.functions(app: FirebaseApp, region: String) =
    rethrow { FirebaseFunctions(getFunctions(app.js, region)) }

actual class FirebaseFunctions internal constructor(val js: Functions) {
    actual fun httpsCallable(name: String, timeout: Long?) =
        rethrow { HttpsCallableReference(httpsCallable(js, name, timeout?.let { json("timeout" to timeout.toDouble()) })) }

    actual fun useEmulator(host: String, port: Int) = connectFunctionsEmulator(js, host, port)
}

@Suppress("UNCHECKED_CAST")
actual class HttpsCallableReference internal constructor(val js: HttpsCallable) {

    actual suspend operator fun invoke() =
        rethrow { HttpsCallableResult(js().await()) }

    actual suspend inline operator fun <reified T> invoke(data: T, encodeDefaults: Boolean) =
        rethrow { HttpsCallableResult(js(encode(data, encodeDefaults)).await()) }

    actual suspend operator fun <T> invoke(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        rethrow { HttpsCallableResult(js(encode(strategy, data, encodeDefaults)).await()) }
}

actual class HttpsCallableResult constructor(val js: JsHttpsCallableResult) {

    actual inline fun <reified T> data() =
        rethrow { decode<T>(value = js.data) }

    actual fun <T> data(strategy: DeserializationStrategy<T>) =
        rethrow { decode(strategy, js.data) }

}

actual class FirebaseFunctionsException(cause: Throwable, val code: FunctionsExceptionCode) : FirebaseException(code.toString(), cause)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val FirebaseFunctionsException.code: FunctionsExceptionCode get() = code

actual enum class FunctionsExceptionCode {
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

inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.functions.rethrow { function() }

inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: dynamic) {
        throw errorToException(e)
    }
}

fun errorToException(e: dynamic) = (e?.code ?: e?.message ?: "")
    .toString()
    .lowercase()
    .let {
        when {
            "cancelled" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.CANCELLED)
            "invalid-argument" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.INVALID_ARGUMENT)
            "deadline-exceeded" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.DEADLINE_EXCEEDED)
            "not-found" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.NOT_FOUND)
            "already-exists" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.ALREADY_EXISTS)
            "permission-denied" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.PERMISSION_DENIED)
            "resource-exhausted" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.RESOURCE_EXHAUSTED)
            "failed-precondition" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.FAILED_PRECONDITION)
            "aborted" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.ABORTED)
            "out-of-range" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.OUT_OF_RANGE)
            "unimplemented" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.UNIMPLEMENTED)
            "internal" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.INTERNAL)
            "unavailable" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.UNAVAILABLE)
            "data-loss" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.DATA_LOSS)
            "unauthenticated" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.UNAUTHENTICATED)
            "unknown" in it -> FirebaseFunctionsException(e, FunctionsExceptionCode.UNKNOWN)
            else -> {
                println("Unknown error code in ${JSON.stringify(e)}")
                FirebaseFunctionsException(e, FunctionsExceptionCode.UNKNOWN)
            }
        }
    }
