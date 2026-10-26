/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.externals.errorCode
import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.json
import dev.gitlive.firebase.externals.toJs
import dev.gitlive.firebase.externals.toKotlin
import dev.gitlive.firebase.functions.SET_TIMEOUT_JVM_ONLY
import dev.gitlive.firebase.functions.WITH_TIMEOUT_JVM_ONLY
import dev.gitlive.firebase.functions.externals.Functions
import dev.gitlive.firebase.functions.externals.HttpsCallable
import dev.gitlive.firebase.functions.externals.connectFunctionsEmulator
import dev.gitlive.firebase.functions.externals.getFunctions
import dev.gitlive.firebase.functions.externals.httpsCallable
import dev.gitlive.firebase.functions.externals.httpsCallableFromURL
import dev.gitlive.firebase.functions.externals.invoke
import kotlin.js.JsException
import kotlin.js.Promise
import dev.gitlive.firebase.functions.externals.HttpsCallableResult as JsHttpsCallableResult

/** @property js The underlying Firebase JS SDK object. */
public actual class FirebaseFunctions internal constructor(public val js: Functions) {

    public actual fun getHttpsCallable(name: String): HttpsCallableReference = HttpsCallableReference(js, name, fromUrl = false, limitedUseAppCheckTokens = false)

    public actual fun getHttpsCallable(name: String, options: HttpsCallableOptions): HttpsCallableReference = HttpsCallableReference(js, name, fromUrl = false, limitedUseAppCheckTokens = options.limitedUseAppCheckTokens)

    internal fun httpsCallableFromUrl(url: String, options: HttpsCallableOptions?): HttpsCallableReference = HttpsCallableReference(js, url, fromUrl = true, limitedUseAppCheckTokens = options?.limitedUseAppCheckTokens ?: false)

    public actual fun useEmulator(host: String, port: Int) {
        rethrow { connectFunctionsEmulator(js, host, port) }
    }

    override fun equals(other: Any?): Boolean = other is FirebaseFunctions && other.js == js

    override fun hashCode(): Int = js.hashCode()

    override fun toString(): String = "FirebaseFunctions($js)"

    public actual companion object {
        public actual fun getInstance(): FirebaseFunctions = rethrow { FirebaseFunctions(getFunctions()) }

        public actual fun getInstance(app: FirebaseApp): FirebaseFunctions = rethrow { FirebaseFunctions(getFunctions(app.js)) }

        public actual fun getInstance(app: FirebaseApp, regionOrCustomDomain: String): FirebaseFunctions = rethrow { FirebaseFunctions(getFunctions(app.js, regionOrCustomDomain)) }

        public actual fun getInstance(regionOrCustomDomain: String): FirebaseFunctions = rethrow { FirebaseFunctions(getFunctions(regionOrCustomDomain = regionOrCustomDomain)) }
    }
}

/**
 * The JS SDK fixes a callable's options, including its timeout, when the callable is created, so the reference keeps
 * what it needs to create one and [js] is created on demand with the current [timeout].
 */
public actual class HttpsCallableReference internal constructor(
    private val functions: Functions,
    private val nameOrUrl: String,
    private val fromUrl: Boolean,
    private val limitedUseAppCheckTokens: Boolean,
    private var timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
) {
    /** The underlying Firebase JS SDK callable, created with the current [timeout]. */
    public val js: HttpsCallable
        get() = rethrow {
            val options = json("timeout" to timeoutMillis.toDouble(), "limitedUseAppCheckTokens" to limitedUseAppCheckTokens)
            if (fromUrl) httpsCallableFromURL(functions, nameOrUrl, options) else httpsCallable(functions, nameOrUrl, options)
        }

    public actual fun call(): Task<HttpsCallableResult> = task { js() }

    public actual fun call(data: Any?): Task<HttpsCallableResult> = task { js(data.toJs()) }

    public actual val timeout: Long get() = timeoutMillis

    @Deprecated(SET_TIMEOUT_JVM_ONLY, level = DeprecationLevel.ERROR)
    public actual fun setTimeout(timeout: Long, units: Any): Unit = throw UnsupportedOperationException(SET_TIMEOUT_JVM_ONLY)

    @Deprecated(WITH_TIMEOUT_JVM_ONLY, level = DeprecationLevel.ERROR)
    public actual fun withTimeout(timeout: Long, units: Any): HttpsCallableReference = throw UnsupportedOperationException(WITH_TIMEOUT_JVM_ONLY)

    internal fun setTimeoutMillis(millis: Long) {
        timeoutMillis = millis
    }

    internal fun withTimeoutMillis(millis: Long): HttpsCallableReference = HttpsCallableReference(functions, nameOrUrl, fromUrl, limitedUseAppCheckTokens, millis)

    override fun toString(): String = "HttpsCallableReference($nameOrUrl)"
}

/**
 * @property js The underlying Firebase JS SDK object. [data] is the JS value with its primitives converted, as the
 * `dev.gitlive` decoders take it on this target.
 */
public actual class HttpsCallableResult internal constructor(public val js: JsHttpsCallableResult) {
    public actual val data: Any? get() = js.data.toKotlin()

    override fun equals(other: Any?): Boolean = other is HttpsCallableResult && other.js == js

    override fun hashCode(): Int = js.hashCode()

    override fun toString(): String = "HttpsCallableResult($data)"
}

private const val DEFAULT_TIMEOUT_MILLIS = 70_000L

private inline fun task(start: () -> Promise<JsHttpsCallableResult>): Task<HttpsCallableResult> {
    val source = TaskCompletionSource<HttpsCallableResult>()
    try {
        start().then(
            onFulfilled = {
                source.setResult(HttpsCallableResult(it))
                null
            },
            onRejected = {
                source.setException(it.toFunctionsException())
                null
            },
        )
    } catch (e: Throwable) {
        source.setException(e.toFunctionsException())
    }
    return source.task
}

private inline fun <R> rethrow(function: () -> R): R = try {
    function()
} catch (e: Throwable) {
    throw e.toFunctionsException()
}

/** The JS SDK reports the canonical error code as `functions/<code-in-kebab-case>`. */
private fun Throwable.toFunctionsException(): FirebaseFunctionsException = when (this) {
    is FirebaseFunctionsException -> this
    is JsException -> functionsException(errorCode(), thrownValue?.let { jsGet(it, "details") }, message, this)
    else -> functionsException(null, null, message, this)
}

/** A promise rejection carries the JS error itself rather than a [Throwable]. */
private fun JsAny.toFunctionsException(): FirebaseFunctionsException {
    val message = jsGet(this, "message").toKotlin()?.toString()
    return functionsException(jsGet(this, "code").toKotlin()?.toString(), jsGet(this, "details"), message, toThrowableOrNull() ?: Exception(message))
}

private fun functionsException(jsCode: String?, details: JsAny?, message: String?, cause: Throwable): FirebaseFunctionsException {
    val codeName = jsCode?.substringAfter('/')?.uppercase()?.replace('-', '_')
    val code = FirebaseFunctionsException.Code.entries.firstOrNull { it.name == codeName } ?: FirebaseFunctionsException.Code.UNKNOWN
    return FirebaseFunctionsException(message ?: jsCode ?: "Unknown error", code, details.toKotlin(), cause)
}
