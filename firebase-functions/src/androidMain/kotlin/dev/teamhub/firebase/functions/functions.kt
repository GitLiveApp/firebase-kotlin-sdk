package dev.teamhub.firebase.functions

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.decode
import dev.teamhub.firebase.encode
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import java.util.concurrent.TimeUnit

@InternalSerializationApi
actual val Firebase.functions
    get() = FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance())

@InternalSerializationApi
actual fun Firebase.functions(region: String) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(region))

@InternalSerializationApi
actual fun Firebase.functions(app: FirebaseApp) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(app.android))

@InternalSerializationApi
actual fun Firebase.functions(app: FirebaseApp, region: String) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(app.android, region))

@InternalSerializationApi
actual class FirebaseFunctions internal constructor(val android: com.google.firebase.functions.FirebaseFunctions) {
    actual fun httpsCallable(name: String, timeout: Long?) =
        HttpsCallableReference(android.getHttpsCallable(name).apply { timeout?.let { setTimeout(it, TimeUnit.MILLISECONDS) } })
}

@InternalSerializationApi
actual class HttpsCallableReference internal constructor(val android: com.google.firebase.functions.HttpsCallableReference) {
    actual suspend fun call() = HttpsCallableResult(android.call().await())

    actual suspend inline fun <reified T> call(data: T) =
        HttpsCallableResult(android.call(encode(data)).await())

    actual suspend inline fun <reified T> call(strategy: SerializationStrategy<T>, data: T) =
        HttpsCallableResult(android.call(encode(strategy, data)).await())
}

actual class HttpsCallableResult constructor(val android: com.google.firebase.functions.HttpsCallableResult) {

    actual inline fun <reified T> data() =
        decode<T>(value = android.data)

    actual inline fun <reified T> data(strategy: DeserializationStrategy<T>) =
        decode(strategy, android.data)
}

actual typealias FirebaseFunctionsException = com.google.firebase.functions.FirebaseFunctionsException