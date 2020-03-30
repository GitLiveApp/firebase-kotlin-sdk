/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.teamhub.firebase.functions

import cocoapods.FirebaseFunctions.*
import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException
import dev.teamhub.firebase.decode
import dev.teamhub.firebase.encode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import platform.Foundation.*

actual val Firebase.functions
    get() = FirebaseFunctions(FIRFunctions.functions())

actual fun Firebase.functions(region: String) =
    FirebaseFunctions(FIRFunctions.functionsForRegion(region))

actual fun Firebase.functions(app: FirebaseApp) =
    FirebaseFunctions(FIRFunctions.functionsForApp(app.ios))

actual fun Firebase.functions(app: FirebaseApp, region: String) =
    FirebaseFunctions(FIRFunctions.functionsForApp(app.ios, region))

actual class FirebaseFunctions internal constructor(val ios: FIRFunctions) {
    actual fun httpsCallable(name: String, timeout: Long?) =
        HttpsCallableReference(ios.HTTPSCallableWithName(name).apply { timeout?.let { setTimeoutInterval(it/1000.0) } })
}

actual class HttpsCallableReference internal constructor(val ios: FIRHTTPSCallable) {
    actual suspend fun call() = HttpsCallableResult(ios.awaitResult { callWithCompletion(it) })

    actual suspend fun call(data: Any) =
        HttpsCallableResult(ios.awaitResult { callWithObject(encode(data), it) })

    actual suspend fun <T> call(strategy: SerializationStrategy<T>, data: T) =
        HttpsCallableResult(ios.awaitResult { callWithObject(encode(strategy, data), it) })
}

actual class HttpsCallableResult constructor(val ios: FIRHTTPSCallableResult) {

    actual inline fun <reified T> data() =
        decode<T>(value = ios.data)

    actual fun <T> data(strategy: DeserializationStrategy<T>) =
        decode(strategy, ios.data)
}

actual class FirebaseFunctionsException(message: String): FirebaseException(message)

private suspend fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(FirebaseFunctionsException(error.toString()))
        }
    }
    job.await()
}

suspend fun <T, R> T.awaitResult(function: T.(callback: (R?, NSError?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R>()
    function { result, error ->
        if(result != null) {
            job.complete(result)
        } else if(error != null) {
            job.completeExceptionally(FirebaseFunctionsException(error.toString()))
        }
    }
    return job.await()
}