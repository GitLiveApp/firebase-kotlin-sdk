package dev.teamhub.firebase.functions

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import kotlinx.coroutines.tasks.await
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
    actual fun getHttpsCallable(name: String, timeout: Long?) =
        HttpsCallableReference(android.getHttpsCallable(name).apply { timeout?.let { setTimeout(it, TimeUnit.MILLISECONDS) } })
}

actual class HttpsCallableReference internal constructor(val android: com.google.firebase.functions.HttpsCallableReference) {
    actual suspend fun call(data: Any?) = HttpsCallableResult(android.call(data).await())
    actual suspend fun call() = HttpsCallableResult(android.call().await())
}

actual class HttpsCallableResult internal constructor(val android: com.google.firebase.functions.HttpsCallableResult) {
    actual val data: Any?
        get() = android.data
}

actual typealias FirebaseFunctionsException = com.google.firebase.functions.FirebaseFunctionsException