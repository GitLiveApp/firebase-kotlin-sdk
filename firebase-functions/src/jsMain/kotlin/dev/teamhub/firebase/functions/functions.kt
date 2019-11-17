package dev.teamhub.firebase.functions

import dev.teamhub.firebase.Firebase
import dev.teamhub.firebase.FirebaseApp
import dev.teamhub.firebase.FirebaseException
import dev.teamhub.firebase.common.firebase
import kotlinx.coroutines.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.DynamicObjectParser
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.stringify
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

val json = Json(JsonConfiguration.Stable)

@Suppress("UNCHECKED_CAST")
actual class HttpsCallableReference internal constructor(val js: firebase.functions.HttpsCallable) {

    actual suspend fun call() =
        rethrow { HttpsCallableResult(js().await()) }

    actual suspend inline fun <reified T : Any> call(data: T) =
        rethrow { HttpsCallableResult(js(JSON.parse(json.stringify(data))).await()) }

    actual suspend inline fun <reified T> call(data: T, strategy: SerializationStrategy<T>) =
        rethrow { HttpsCallableResult(js(JSON.parse(json.stringify(strategy, data))).await()) }
}

actual class HttpsCallableResult constructor(val js: firebase.functions.HttpsCallableResult) {

    actual inline fun <reified T: Any> data() =
        rethrow { DynamicObjectParser().parse<T>(js.data) }

    actual inline fun <reified T> data(strategy: DeserializationStrategy<T>) =
        rethrow { DynamicObjectParser().parse(js.data, strategy) }

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