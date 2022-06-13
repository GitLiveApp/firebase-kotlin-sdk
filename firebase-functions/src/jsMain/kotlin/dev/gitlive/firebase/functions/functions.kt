/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import dev.gitlive.firebase.*
import dev.gitlive.firebase.externals.functions.*
import kotlinx.coroutines.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.w3c.dom.url.URL
import kotlin.js.json
import dev.gitlive.firebase.externals.functions.HttpsCallableResult as JsHttpsCallableResult

actual val Firebase.functions: FirebaseFunctions
    get() = rethrow { FirebaseFunctions(getFunctions()) }

actual fun Firebase.functions(region: String) =
    rethrow { FirebaseFunctions(getFunctions(regionOrCustomDomain = region)) }

actual fun Firebase.functions(app: FirebaseApp) =
    rethrow { FirebaseFunctions(getFunctions(app.js)) }

actual fun Firebase.functions(app: FirebaseApp, region: String) =
    rethrow { FirebaseFunctions(getFunctions(app.js, region)) }

actual class FirebaseFunctions internal constructor(val js: Functions) {
    actual fun httpsCallable(name: String, timeout: Long?) =
        rethrow { HttpsCallableReference(httpsCallable(js, name, timeout?.let { json("timeout" to timeout.toDouble()) })) }

    actual fun useFunctionsEmulator(origin: String) {
        val url = URL(origin)
        useEmulator(url.host, url.port.toInt())
    }

    actual fun useEmulator(host: String, port: Int) = connectFunctionsEmulator(js, host, port)
}

@Suppress("UNCHECKED_CAST")
actual class HttpsCallableReference internal constructor(val js: HttpsCallable) {

    actual suspend operator fun invoke() =
        rethrow { HttpsCallableResult(js().await()) }

    actual suspend inline operator fun <reified T> invoke(data: T, encodeDefaults: Boolean) =
        rethrow { HttpsCallableResult(js(encode(data, encodeDefaults)).await()) }

    actual suspend operator fun <T> invoke(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) =
        rethrow { HttpsCallableResult(js(encode(strategy, data, encodeDefaults)).await()) }
}

actual class HttpsCallableResult constructor(val js: JsHttpsCallableResult) {

    actual inline fun <reified T> data() =
        rethrow { decode<T>(value = js.data) }

    actual fun <T> data(strategy: DeserializationStrategy<T>) =
        rethrow { decode(strategy, js.data) }

}

actual open class FirebaseFunctionsException(code: String?, cause: Throwable): FirebaseException(code, cause)

inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.functions.rethrow { function() }

inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: dynamic) {
        throw FirebaseFunctionsException(e.code as String?, e)
    }
}
