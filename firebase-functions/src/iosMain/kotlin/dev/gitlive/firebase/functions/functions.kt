/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import cocoapods.FirebaseFunctions.*
import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.decode
import dev.gitlive.firebase.encode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import platform.Foundation.*

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

actual class FirebaseFunctionsException(message: String): FirebaseException(message)

suspend inline fun <T> T.await(function: T.(callback: (NSError?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    val callback = { error: NSError? ->
        if(error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(FirebaseFunctionsException(error.localizedDescription))
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
            job.completeExceptionally(FirebaseFunctionsException(error.localizedDescription))
        }
    }
    function(callback)
    return job.await() as R
}
