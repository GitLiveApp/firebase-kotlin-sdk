package dev.teamhub.firebase.functions

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException
import dev.teamhub.firebase.common.firebase
import dev.teamhub.firebase.common.fromJson
import dev.teamhub.firebase.common.toJson
import kotlinx.coroutines.await
import kotlin.js.json

actual val Firebase.functions: FirebaseFunctions
    get() = rethrow { firebase.functions; FirebaseFunctions(firebase.functions()) }

actual fun Firebase.functions(region: String) =
    rethrow { functions; FirebaseFunctions(firebase.app().functions(region)) }

actual fun Firebase.functions(app: FirebaseApp) =
    rethrow { functions; FirebaseFunctions(firebase.functions(app.js)) }

actual fun Firebase.functions(app: FirebaseApp, region: String) =
    rethrow { functions; FirebaseFunctions(app.js.functions(region)) }

actual class FirebaseFunctions internal constructor(val js: firebase.functions.Functions) {
    actual fun getHttpsCallable(name: String, timeout: Long?) =
        rethrow { HttpsCallableReference(js.httpsCallable(name, timeout?.let { json("timeout" to timeout.toDouble()) })) }
}

actual class HttpsCallableReference internal constructor(val js: firebase.functions.HttpsCallable) {
    actual suspend fun call(data: Any?) =
        rethrow { HttpsCallableResult(js(toJson(data)).await()) }
    actual suspend fun call() =
        rethrow { HttpsCallableResult(js().await()) }
}

actual class HttpsCallableResult internal constructor(val js: firebase.functions.HttpsCallableResult) {
    actual val data: Any?
        get() = rethrow { fromJson(js.data) }
}

actual open class FirebaseFunctionsException(code: String?, message: String?): FirebaseException(code, message)

private inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.teamhub.firebase.functions.rethrow { function() }

private inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: Throwable) {
        throw FirebaseFunctionsException(e.asDynamic().code as String?, e.message)
    }
}