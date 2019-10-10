package dev.teamhub.firebase.functions

import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

actual fun getFirebaseFunctions() = FirebaseFunctions.getInstance()

actual typealias FirebaseFunctions = com.google.firebase.functions.FirebaseFunctions

actual typealias HttpsCallableReference = com.google.firebase.functions.HttpsCallableReference

actual suspend fun HttpsCallableReference.awaitCall(data: Any?) = call(data).await()
actual suspend fun HttpsCallableReference.awaitCall() = call().await()

actual typealias HttpsCallableResult = com.google.firebase.functions.HttpsCallableResult

actual val HttpsCallableResult.data: Any
    get() = data

actual fun FirebaseFunctions.getHttpsCallable(name: String, timeout: Long?) =
    getHttpsCallable(name).apply { timeout?.let { setTimeout(it, TimeUnit.MILLISECONDS) } }

actual typealias FirebaseFunctionsException = com.google.firebase.functions.FirebaseFunctionsException