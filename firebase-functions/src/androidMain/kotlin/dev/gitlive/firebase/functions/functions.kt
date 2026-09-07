/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("AndroidFunctions")

package dev.gitlive.firebase.functions

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.android
import dev.gitlive.firebase.functions.android as publicAndroid
import dev.gitlive.firebase.internal.decode
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

public val FirebaseFunctions.android: dev.gitlive.firebase.android.functions.FirebaseFunctions get() = dev.gitlive.firebase.android.functions.FirebaseFunctions.getInstance()

public actual val Firebase.functions: FirebaseFunctions
    get() = FirebaseFunctions(dev.gitlive.firebase.android.functions.FirebaseFunctions.getInstance())

public actual fun Firebase.functions(region: String): FirebaseFunctions = FirebaseFunctions(dev.gitlive.firebase.android.functions.FirebaseFunctions.getInstance(region))

public actual fun Firebase.functions(app: FirebaseApp): FirebaseFunctions = FirebaseFunctions(dev.gitlive.firebase.android.functions.FirebaseFunctions.getInstance(app.android))

public actual fun Firebase.functions(app: FirebaseApp, region: String): FirebaseFunctions = FirebaseFunctions(dev.gitlive.firebase.android.functions.FirebaseFunctions.getInstance(app.android, region))

public actual data class FirebaseFunctions internal constructor(internal val android: dev.gitlive.firebase.android.functions.FirebaseFunctions) {
    public actual fun httpsCallable(name: String, timeout: Duration?): HttpsCallableReference = HttpsCallableReference(android.getHttpsCallable(name).apply { timeout?.let { setTimeout(it.inWholeMilliseconds, TimeUnit.MILLISECONDS) } }.native)

    public actual fun useEmulator(host: String, port: Int) {
        android.useEmulator(host, port)
    }
}

@PublishedApi
internal actual data class NativeHttpsCallableReference(val android: dev.gitlive.firebase.android.functions.HttpsCallableReference) {
    actual suspend fun invoke(encodedData: Any): HttpsCallableResult = HttpsCallableResult(android.call(encodedData).await())
    actual suspend fun invoke(): HttpsCallableResult = HttpsCallableResult(android.call().await())
}

internal val dev.gitlive.firebase.android.functions.HttpsCallableReference.native get() = NativeHttpsCallableReference(this)

internal val HttpsCallableReference.android: dev.gitlive.firebase.android.functions.HttpsCallableReference get() = native.android
public val HttpsCallableResult.android: dev.gitlive.firebase.android.functions.HttpsCallableResult get() = android

public actual class HttpsCallableResult(internal val android: dev.gitlive.firebase.android.functions.HttpsCallableResult) {

    public actual inline fun <reified T> data(): T = decode<T>(value = publicAndroid.data)

    public actual inline fun <T> data(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit): T = decode(strategy, publicAndroid.data, buildSettings)
}

public actual typealias FirebaseFunctionsException = dev.gitlive.firebase.android.functions.FirebaseFunctionsException

@Suppress("ConflictingExtensionProperty")
public actual val FirebaseFunctionsException.code: FunctionsExceptionCode get() = code

@Suppress("ConflictingExtensionProperty")
public actual val FirebaseFunctionsException.details: Any? get() = details

public actual typealias FunctionsExceptionCode = dev.gitlive.firebase.android.functions.FirebaseFunctionsException.Code
