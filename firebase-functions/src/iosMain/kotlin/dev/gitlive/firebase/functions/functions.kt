/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import cocoapods.FirebaseFunctions.FIRFunctions
import cocoapods.FirebaseFunctions.FIRHTTPSCallable
import cocoapods.FirebaseFunctions.FIRHTTPSCallableResult
import dev.gitlive.firebase.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import platform.Foundation.NSError

actual val Firebase.functions
    get() = FirebaseFunctions(FIRFunctions.functions())

actual fun Firebase.functions(region: String) =
    FirebaseFunctions(FIRFunctions.functionsForRegion(region))

@Suppress("CAST_NEVER_SUCCEEDS")
actual fun Firebase.functions(app: FirebaseApp): FirebaseFunctions = FirebaseFunctions(
    FIRFunctions.functionsForApp(app.ios as objcnames.classes.FIRApp)
)

@Suppress("CAST_NEVER_SUCCEEDS")
actual fun Firebase.functions(
    app: FirebaseApp,
    region: String,
): FirebaseFunctions = FirebaseFunctions(
    FIRFunctions.functionsForApp(app.ios as objcnames.classes.FIRApp, region = region)
)

actual data class FirebaseFunctions internal constructor(val ios: FIRFunctions) {
    actual fun httpsCallable(name: String, timeout: Long?) =
        HttpsCallableReference(ios.HTTPSCallableWithName(name).apply { timeout?.let { setTimeoutInterval(it/1000.0) } })

    actual fun useEmulator(host: String, port: Int) = ios.useEmulatorWithHost(host, port.toLong())
}

actual class HttpsCallableReference internal constructor(val ios: FIRHTTPSCallable) : BaseHttpsCallableReference() {
    actual suspend operator fun invoke() = HttpsCallableResult(ios.awaitResult { callWithCompletion(it) })

    override suspend fun invoke(encodedData: Any): HttpsCallableResult = HttpsCallableResult(ios.awaitResult { callWithObject(encodedData, it) })
}

actual class HttpsCallableResult constructor(val ios: FIRHTTPSCallableResult) {

    actual inline fun <reified T> data() =
        decode<T>(value = ios.data())

    actual fun <T> data(strategy: DeserializationStrategy<T>, decodeSettings: DecodeSettings) =
        decode(strategy, ios.data(), decodeSettings)
}

actual class FirebaseFunctionsException(message: String, val code: FunctionsExceptionCode, val details: Any?) : FirebaseException(message)

actual val FirebaseFunctionsException.code: FunctionsExceptionCode get() = code

actual val FirebaseFunctionsException.details: Any? get() = details

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
//todo uncomment once https://github.com/firebase/firebase-ios-sdk/issues/11862 fixed
fun NSError.toException() = when(domain) {
//    FIRFunctionsErrorDomain -> when(code) {
//        FIRFunctionsErrorCodeOK -> FunctionsExceptionCode.OK
//        FIRFunctionsErrorCodeCancelled -> FunctionsExceptionCode.CANCELLED
//        FIRFunctionsErrorCodeUnknown -> FunctionsExceptionCode.UNKNOWN
//        FIRFunctionsErrorCodeInvalidArgument -> FunctionsExceptionCode.INVALID_ARGUMENT
//        FIRFunctionsErrorCodeDeadlineExceeded -> FunctionsExceptionCode.DEADLINE_EXCEEDED
//        FIRFunctionsErrorCodeNotFound -> FunctionsExceptionCode.NOT_FOUND
//        FIRFunctionsErrorCodeAlreadyExists -> FunctionsExceptionCode.ALREADY_EXISTS
//        FIRFunctionsErrorCodePermissionDenied -> FunctionsExceptionCode.PERMISSION_DENIED
//        FIRFunctionsErrorCodeResourceExhausted -> FunctionsExceptionCode.RESOURCE_EXHAUSTED
//        FIRFunctionsErrorCodeFailedPrecondition -> FunctionsExceptionCode.FAILED_PRECONDITION
//        FIRFunctionsErrorCodeAborted -> FunctionsExceptionCode.ABORTED
//        FIRFunctionsErrorCodeOutOfRange -> FunctionsExceptionCode.OUT_OF_RANGE
//        FIRFunctionsErrorCodeUnimplemented -> FunctionsExceptionCode.UNIMPLEMENTED
//        FIRFunctionsErrorCodeInternal -> FunctionsExceptionCode.INTERNAL
//        FIRFunctionsErrorCodeUnavailable -> FunctionsExceptionCode.UNAVAILABLE
//        FIRFunctionsErrorCodeDataLoss -> FunctionsExceptionCode.DATA_LOSS
//        FIRFunctionsErrorCodeUnauthenticated -> FunctionsExceptionCode.UNAUTHENTICATED
//        else -> FunctionsExceptionCode.UNKNOWN
//    }
    else -> FunctionsExceptionCode.UNKNOWN
}.let { FirebaseFunctionsException(description!!, it, null/*userInfo[FIRFunctionsErrorDetails]*/) }

suspend inline fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    val callback = { error: NSError? ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    function(callback)
    job.await()
}

suspend inline fun <T, reified R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    val callback = { result: R?, error: NSError? ->
        if(error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    function(callback)
    return job.await() as R
}
