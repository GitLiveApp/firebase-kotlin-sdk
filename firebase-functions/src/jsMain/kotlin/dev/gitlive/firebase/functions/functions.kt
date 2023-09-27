/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import dev.gitlive.firebase.*
import dev.gitlive.firebase.functions.externals.*
import kotlinx.coroutines.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.w3c.dom.url.URL
import kotlin.js.json
import dev.gitlive.firebase.functions.externals.HttpsCallableResult as JsHttpsCallableResult

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

    actual fun useEmulator(host: String, port: Int) = connectFunctionsEmulator(js, host, port)
}

@Suppress("UNCHECKED_CAST")
actual class HttpsCallableReference internal constructor(val js: HttpsCallable) : BaseHttpsCallableReference() {

    actual suspend operator fun invoke() =
        rethrow { HttpsCallableResult(js().await()) }

    override suspend fun invoke(encodedData: Any): HttpsCallableResult = rethrow {
        HttpsCallableResult(js(encodedData).await())
    }
}

actual class HttpsCallableResult constructor(val js: JsHttpsCallableResult) {

    actual inline fun <reified T> data() =
        rethrow { decode<T>(value = js.data) }

    actual fun <T> data(strategy: DeserializationStrategy<T>, decodeSettings: DecodeSettings) =
        rethrow { decode(strategy, js.data, decodeSettings) }

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
