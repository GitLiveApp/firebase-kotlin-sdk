package dev.teamhub.firebase.functions

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException
import dev.teamhub.firebase.common.firebase
import kotlinx.coroutines.await
import kotlinx.serialization.DynamicObjectParser
import kotlinx.serialization.Mapper
import kotlin.js.json

actual val Firebase.functions: FirebaseFunctions
    get() = rethrow { dev.teamhub.firebase.common.functions; FirebaseFunctions(firebase.functions()) }

actual fun Firebase.functions(region: String) =
    rethrow { dev.teamhub.firebase.common.functions; FirebaseFunctions(firebase.app().functions(region)) }

actual fun Firebase.functions(app: FirebaseApp) =
    rethrow { dev.teamhub.firebase.common.functions; FirebaseFunctions(firebase.functions(app.js)) }

actual fun Firebase.functions(app: FirebaseApp, region: String) =
    rethrow { dev.teamhub.firebase.common.functions; FirebaseFunctions(app.js.functions(region)) }

actual class FirebaseFunctions internal constructor(val js: firebase.functions.Functions) {
    actual fun httpsCallable(name: String, timeout: Long?) =
        rethrow { HttpsCallableReference(js.httpsCallable(name, timeout?.let { json("timeout" to timeout.toDouble()) })) }
}

@Suppress("UNCHECKED_CAST")
actual class HttpsCallableReference internal constructor(val js: firebase.functions.HttpsCallable) {
    actual suspend fun call() =
        rethrow { HttpsCallableResult(js().await()) }

    actual suspend inline fun <reified T : Any> call(data: T) =
        rethrow { HttpsCallableResult(js((data as? Map<String, Any>)?.let { Mapper.map(it) } ?: data).await()) }
}

actual class HttpsCallableResult constructor(val js: firebase.functions.HttpsCallableResult) {

    actual inline fun <reified T: Any> get() =
        rethrow { js.data?.let { DynamicObjectParser().parse<T>(it) } }

    actual inline fun <reified T : Any> getList(): List<T>? =
        rethrow { js.data?.unsafeCast<Array<*>>()?.map { DynamicObjectParser().parse<T>(it) } }
}

actual open class FirebaseFunctionsException(code: String?, message: String?): FirebaseException(code, message)

inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.teamhub.firebase.functions.rethrow { function() }

inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: Throwable) {
        throw FirebaseFunctionsException(e.asDynamic().code as String?, e.message)
    }
}