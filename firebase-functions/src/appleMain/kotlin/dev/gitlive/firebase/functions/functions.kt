/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package dev.gitlive.firebase.functions

import cocoapods.FirebaseFunctions.FIRFunctions
import cocoapods.FirebaseFunctions.FIRHTTPSCallable
import cocoapods.FirebaseFunctions.FIRHTTPSCallableResult
import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.functions.ios as publicIos
import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.ios
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.DeserializationStrategy
import platform.Foundation.NSError
import kotlin.time.Duration
import kotlin.time.DurationUnit

public val FirebaseFunctions.ios: FIRFunctions get() = FIRFunctions.functions()

public actual val Firebase.functions: FirebaseFunctions
    get() = FirebaseFunctions(FIRFunctions.functions())

public actual fun Firebase.functions(region: String): FirebaseFunctions = FirebaseFunctions(FIRFunctions.functionsForRegion(region))

public actual fun Firebase.functions(app: FirebaseApp): FirebaseFunctions = FirebaseFunctions(
    FIRFunctions.functionsForApp(app.ios as objcnames.classes.FIRApp),
)

public actual fun Firebase.functions(
    app: FirebaseApp,
    region: String,
): FirebaseFunctions = FirebaseFunctions(
    FIRFunctions.functionsForApp(app.ios as objcnames.classes.FIRApp, region = region),
)

public actual data class FirebaseFunctions internal constructor(internal val ios: FIRFunctions) {
    public actual fun httpsCallable(name: String, timeout: Duration?): HttpsCallableReference = HttpsCallableReference(ios.HTTPSCallableWithName(name).apply { timeout?.let { setTimeoutInterval(it.toDouble(DurationUnit.SECONDS)) } }.native)

    public actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host, port.toLong())
    }
}

@PublishedApi
internal actual data class NativeHttpsCallableReference(val ios: FIRHTTPSCallable) {
    actual suspend fun invoke(encodedData: Any): HttpsCallableResult = HttpsCallableResult(ios.awaitResult { callWithObject(encodedData, it) })
    actual suspend fun invoke(): HttpsCallableResult = HttpsCallableResult(ios.awaitResult { callWithCompletion(it) })
}

internal val FIRHTTPSCallable.native get() = NativeHttpsCallableReference(this)

internal val HttpsCallableReference.ios: FIRHTTPSCallable get() = native.ios
public val HttpsCallableResult.ios: FIRHTTPSCallableResult get() = ios

public actual class HttpsCallableResult(internal val ios: FIRHTTPSCallableResult) {

    public actual inline fun <reified T> data(): T = decode<T>(value = publicIos.data())

    public actual inline fun <T> data(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit): T = decode(strategy, publicIos.data(), buildSettings)
}

public actual class FirebaseFunctionsException(message: String, public val code: FunctionsExceptionCode, public val details: Any?) : FirebaseException(message)

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

private const val FUNCTIONS_ERROR_DOMAIN = "com.firebase.functions"
private const val FUNCTIONS_ERROR_DETAILS_KEY = "details"

internal fun NSError.toException() = when (domain) {
    FUNCTIONS_ERROR_DOMAIN -> when (code) {
        0L -> FunctionsExceptionCode.OK
        1L -> FunctionsExceptionCode.CANCELLED
        2L -> FunctionsExceptionCode.UNKNOWN
        3L -> FunctionsExceptionCode.INVALID_ARGUMENT
        4L -> FunctionsExceptionCode.DEADLINE_EXCEEDED
        5L -> FunctionsExceptionCode.NOT_FOUND
        6L -> FunctionsExceptionCode.ALREADY_EXISTS
        7L -> FunctionsExceptionCode.PERMISSION_DENIED
        8L -> FunctionsExceptionCode.RESOURCE_EXHAUSTED
        9L -> FunctionsExceptionCode.FAILED_PRECONDITION
        10L -> FunctionsExceptionCode.ABORTED
        11L -> FunctionsExceptionCode.OUT_OF_RANGE
        12L -> FunctionsExceptionCode.UNIMPLEMENTED
        13L -> FunctionsExceptionCode.INTERNAL
        14L -> FunctionsExceptionCode.UNAVAILABLE
        15L -> FunctionsExceptionCode.DATA_LOSS
        16L -> FunctionsExceptionCode.UNAUTHENTICATED
        else -> FunctionsExceptionCode.UNKNOWN
    }
    else -> FunctionsExceptionCode.UNKNOWN
}.let {
    FirebaseFunctionsException(
        localizedDescription,
        it,
        userInfo[FUNCTIONS_ERROR_DETAILS_KEY],
    )
}

internal suspend inline fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error ->
        if (error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    job.await()
}

internal suspend inline fun <T, reified R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    function { result, error ->
        if (error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    return job.await() as R
}
