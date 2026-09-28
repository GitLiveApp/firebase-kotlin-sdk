/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.functions

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.functions.SET_TIMEOUT_JVM_ONLY
import dev.gitlive.firebase.functions.WITH_TIMEOUT_JVM_ONLY
import dev.gitlive.firebase.functions.externals.Functions
import dev.gitlive.firebase.functions.externals.HttpsCallable
import dev.gitlive.firebase.functions.externals.connectFunctionsEmulator
import dev.gitlive.firebase.functions.externals.getFunctions
import dev.gitlive.firebase.functions.externals.httpsCallable
import dev.gitlive.firebase.functions.externals.httpsCallableFromURL
import dev.gitlive.firebase.functions.externals.invoke
import kotlin.js.Promise
import kotlin.js.json
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

/** @property js The underlying Firebase JS SDK object. */
public actual class HttpsCallableResult internal constructor(public val js: JsHttpsCallableResult) {
    public actual val data: Any? get() = js.data

    override fun equals(other: Any?): Boolean = other is HttpsCallableResult && other.js == js

    override fun hashCode(): Int = js.hashCode()

    override fun toString(): String = "HttpsCallableResult($data)"
}

private const val DEFAULT_TIMEOUT_MILLIS = 70_000L

private inline fun task(start: () -> Promise<JsHttpsCallableResult>): Task<HttpsCallableResult> {
    val source = TaskCompletionSource<HttpsCallableResult>()
    try {
        start().then({ source.setResult(HttpsCallableResult(it)) }, { source.setException(it.toFunctionsException()) })
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

/** Kotlin collections passed as call data become JS objects and arrays, as the JS SDK serializes them to JSON. */
private fun Any?.toJs(): Any? = when (this) {
    is Map<*, *> -> json(*entries.map { (key, value) -> key.toString() to value.toJs() }.toTypedArray())
    is Collection<*> -> map { it.toJs() }.toTypedArray()
    is Array<*> -> map { it.toJs() }.toTypedArray()
    is Long -> toDouble()
    else -> this
}

/** The JS SDK reports the canonical error code as `functions/<code-in-kebab-case>`. */
private fun Throwable.toFunctionsException(): FirebaseFunctionsException {
    if (this is FirebaseFunctionsException) return this
    val jsCode = asDynamic().code.unsafeCast<String?>()
    val codeName = jsCode?.substringAfter('/')?.uppercase()?.replace('-', '_')
    val code = FirebaseFunctionsException.Code.entries.firstOrNull { it.name == codeName } ?: FirebaseFunctionsException.Code.UNKNOWN
    return FirebaseFunctionsException(message ?: jsCode ?: "Unknown error", code, asDynamic().details.unsafeCast<Any?>(), this)
}
