/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.internal.decode
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import java.util.concurrent.TimeUnit

actual val Firebase.functions
    get() = FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance())

actual fun Firebase.functions(region: String) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(region))

actual fun Firebase.functions(app: FirebaseApp) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(app.android))

actual fun Firebase.functions(app: FirebaseApp, region: String) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(app.android, region))

actual data class FirebaseFunctions internal constructor(val android: com.google.firebase.functions.FirebaseFunctions) {
    actual fun httpsCallable(name: String, timeout: Long?) =
        HttpsCallableReference(android.getHttpsCallable(name).apply { timeout?.let { setTimeout(it, TimeUnit.MILLISECONDS) } }.native)

    actual fun useEmulator(host: String, port: Int) = android.useEmulator(host, port)
}

@PublishedApi
internal actual data class NativeHttpsCallableReference(val android: com.google.firebase.functions.HttpsCallableReference) {
    actual suspend fun invoke(encodedData: Any): HttpsCallableResult = HttpsCallableResult(android.call(encodedData).await())
    actual suspend fun invoke(): HttpsCallableResult = HttpsCallableResult(android.call().await())
}

internal val com.google.firebase.functions.HttpsCallableReference.native get() = NativeHttpsCallableReference(this)

val HttpsCallableReference.android: com.google.firebase.functions.HttpsCallableReference get() = native.android

actual class HttpsCallableResult constructor(val android: com.google.firebase.functions.HttpsCallableResult) {

    actual inline fun <reified T> data() =
        decode<T>(value = android.data)

    actual inline fun <T> data(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit) =
        decode(strategy, android.data, buildSettings)
}

actual typealias FirebaseFunctionsException = com.google.firebase.functions.FirebaseFunctionsException

actual val FirebaseFunctionsException.code: FunctionsExceptionCode get() = code

actual val FirebaseFunctionsException.details: Any? get() = details

actual typealias FunctionsExceptionCode = com.google.firebase.functions.FirebaseFunctionsException.Code
