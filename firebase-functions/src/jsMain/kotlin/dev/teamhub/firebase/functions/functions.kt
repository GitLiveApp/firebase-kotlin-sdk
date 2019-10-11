package dev.teamhub.firebase.functions

import dev.teamhub.firebase.FirebaseException
import dev.teamhub.firebase.common.fromJson
import dev.teamhub.firebase.common.toJson
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json

actual fun getFirebaseFunctions() = rethrow { functions; firebase.functions() }

actual typealias FirebaseFunctions = firebase.functions.Functions
actual typealias HttpsCallableResult = firebase.functions.HttpsCallableResult
actual typealias HttpsCallableReference = firebase.functions.HttpsCallable

actual val HttpsCallableResult.data: Any
    get() = rethrow { fromJson(asDynamic().data as Any)!! }

actual suspend fun HttpsCallableReference.awaitCall(data: Any?) = rethrow { this.asDynamic()(toJson(data)).unsafeCast<Promise<HttpsCallableResult>>().await() }

actual suspend fun HttpsCallableReference.awaitCall() = rethrow { this.asDynamic()().unsafeCast<Promise<HttpsCallableResult>>().await() }

actual fun FirebaseFunctions.getHttpsCallable(name: String, timeout: Long?) = rethrow { httpsCallable(name, timeout?.let { json("timeout" to timeout.toDouble()) }) }

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