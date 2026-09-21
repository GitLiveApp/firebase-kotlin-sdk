/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import com.google.firebase.perf.metrics.Trace
import dev.gitlive.firebase.perf.FirebasePerformanceException
import dev.gitlive.firebase.perf.externals.getPerformance
import dev.gitlive.firebase.perf.externals.trace
import dev.gitlive.firebase.perf.externals.FirebasePerformance as JsFirebasePerformance

/** @property js The underlying Firebase JS SDK object. */
public actual class FirebasePerformance internal constructor(public val js: JsFirebasePerformance) {
    public actual var isPerformanceCollectionEnabled: Boolean
        get() = js.dataCollectionEnabled
        set(value) {
            js.dataCollectionEnabled = value
        }

    public actual fun newTrace(traceName: String): Trace = rethrow { Trace(trace(js, traceName)) }

    override fun equals(other: Any?): Boolean = other is FirebasePerformance && other.js == js

    override fun hashCode(): Int = js.hashCode()

    override fun toString(): String = "FirebasePerformance($js)"

    @Retention(AnnotationRetention.SOURCE)
    public actual annotation class HttpMethod {
        public actual companion object {
            public actual val CONNECT: String = "CONNECT"
            public actual val DELETE: String = "DELETE"
            public actual val GET: String = "GET"
            public actual val HEAD: String = "HEAD"
            public actual val OPTIONS: String = "OPTIONS"
            public actual val PATCH: String = "PATCH"
            public actual val POST: String = "POST"
            public actual val PUT: String = "PUT"
            public actual val TRACE: String = "TRACE"
        }
    }

    public actual companion object {
        /** Maximum length of a trace name. */
        public actual val MAX_TRACE_NAME_LENGTH: Int = 100

        public actual fun getInstance(): FirebasePerformance = rethrow { FirebasePerformance(getPerformance()) }

        public actual fun startTrace(traceName: String): Trace = getInstance().newTrace(traceName).apply { start() }
    }
}

internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch (e: dynamic) {
        throw errorToException(e)
    }
}

internal fun errorToException(error: dynamic): FirebasePerformanceException = (error?.code ?: error?.message ?: "")
    .toString()
    .lowercase()
    .let { code -> FirebasePerformanceException(code, error as Throwable) }
