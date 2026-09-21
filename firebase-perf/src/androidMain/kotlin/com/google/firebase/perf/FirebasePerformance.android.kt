/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import com.google.firebase.perf.metrics.HttpMetric
import com.google.firebase.perf.metrics.Trace
import dev.gitlive.firebase.perf.stub
import kotlin.jvm.JvmField

/*
 * Header stubs for com.google.firebase:firebase-perf (see buildSrc utils/HeaderStubs.kt): compiled against, verified
 * to match the real classes, and deleted from the output so the real SDK binds at runtime.
 */

public actual class FirebasePerformance private constructor() {
    public actual var isPerformanceCollectionEnabled: Boolean
        get() = stub()
        set(_) = stub()

    public actual fun newTrace(traceName: String): Trace = stub()

    /** Not in the common API (JS has no [HttpMetric]); the shipped nonJsMain [newHttpMetric] extension binds to this member. */
    public fun newHttpMetric(url: String, httpMethod: String): HttpMetric = stub()

    @Retention(AnnotationRetention.SOURCE)
    public actual annotation class HttpMethod {
        public actual companion object {
            @JvmField
            public actual val CONNECT: String = "CONNECT"

            @JvmField
            public actual val DELETE: String = "DELETE"

            @JvmField
            public actual val GET: String = "GET"

            @JvmField
            public actual val HEAD: String = "HEAD"

            @JvmField
            public actual val OPTIONS: String = "OPTIONS"

            @JvmField
            public actual val PATCH: String = "PATCH"

            @JvmField
            public actual val POST: String = "POST"

            @JvmField
            public actual val PUT: String = "PUT"

            @JvmField
            public actual val TRACE: String = "TRACE"
        }
    }

    public actual companion object {
        /** Maximum length of a trace name. */
        @JvmField
        public actual val MAX_TRACE_NAME_LENGTH: Int = 100

        @JvmStatic
        public actual fun getInstance(): FirebasePerformance = stub()

        @JvmStatic
        public actual fun startTrace(traceName: String): Trace = stub()
    }
}
