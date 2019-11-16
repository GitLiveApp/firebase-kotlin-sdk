package dev.teamhub.firebase.functions

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Mapper
import java.util.concurrent.TimeUnit

actual val Firebase.functions
    get() = FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance())

actual fun Firebase.functions(region: String) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(region))

actual fun Firebase.functions(app: FirebaseApp) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(app.android))

actual fun Firebase.functions(app: FirebaseApp, region: String) =
    FirebaseFunctions(com.google.firebase.functions.FirebaseFunctions.getInstance(app.android, region))

actual class FirebaseFunctions internal constructor(val android: com.google.firebase.functions.FirebaseFunctions) {
    actual fun httpsCallable(name: String, timeout: Long?) =
        HttpsCallableReference(android.getHttpsCallable(name).apply { timeout?.let { setTimeout(it, TimeUnit.MILLISECONDS) } })
}

@Suppress("UNCHECKED_CAST")
actual class HttpsCallableReference internal constructor(val android: com.google.firebase.functions.HttpsCallableReference) {
    actual suspend fun call() = HttpsCallableResult(android.call().await())

    actual suspend inline fun <reified T : Any> call(data: T) =
        HttpsCallableResult(android.call((data as? Map<String, Any>)?.let { Mapper.map(it) } ?: data).await())
}

@Suppress("UNCHECKED_CAST")
actual class HttpsCallableResult constructor(val android: com.google.firebase.functions.HttpsCallableResult) {

    actual inline fun <reified T: Any> get() = when(T::class) {
        Boolean::class -> android.data as T?
        String::class -> android.data as T?
        Long::class -> android.data as T?
        else -> android.data?.let { Mapper.unmap<T>(it as Map<String, Any>) }
    }

    actual inline fun <reified T: Any> getList() = when(T::class) {
        Boolean::class -> android.data as List<T>?
        String::class -> android.data as List<T>?
        Long::class -> android.data as List<T>?
        else -> (android.data as List<Any>?)?.map { Mapper.unmap<T>(it as Map<String, Any>) }
    }
}

actual typealias FirebaseFunctionsException = com.google.firebase.functions.FirebaseFunctionsException