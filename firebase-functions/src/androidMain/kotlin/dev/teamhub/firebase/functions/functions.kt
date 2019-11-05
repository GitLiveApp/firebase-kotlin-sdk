package dev.teamhub.firebase.functions

import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import org.json.JSONTokener
import java.util.concurrent.TimeUnit

private val gson by lazy { Gson() }

actual fun getFirebaseFunctions() = FirebaseFunctions.getInstance()

actual typealias FirebaseFunctions = com.google.firebase.functions.FirebaseFunctions

actual typealias HttpsCallableReference = com.google.firebase.functions.HttpsCallableReference

actual suspend fun HttpsCallableReference.awaitCall(data: Any?) = call(JSONTokener(gson.toJson(data)).nextValue()).await()
actual suspend fun HttpsCallableReference.awaitCall() = call().await()

actual typealias HttpsCallableResult = com.google.firebase.functions.HttpsCallableResult

actual val HttpsCallableResult.data: Any
    get() = data

actual fun FirebaseFunctions.getHttpsCallable(name: String, timeout: Long?) =
    getHttpsCallable(name).apply { timeout?.let { setTimeout(it, TimeUnit.MILLISECONDS) } }

actual typealias FirebaseFunctionsException = com.google.firebase.functions.FirebaseFunctionsException
