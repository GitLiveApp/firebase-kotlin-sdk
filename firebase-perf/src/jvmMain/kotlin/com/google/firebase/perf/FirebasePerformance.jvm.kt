/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import com.google.firebase.perf.metrics.Trace
import kotlin.jvm.JvmField

/*
 * firebase-java-sdk has no Performance Monitoring, so on the JVM these classes are real: they keep the metrics and
 * attributes of traces in memory, without reporting anything.
 */

public actual class FirebasePerformance private constructor() {
    public actual var isPerformanceCollectionEnabled: Boolean = true

    public actual fun newTrace(traceName: String): Trace = Trace(traceName)

    override fun toString(): String = "FirebasePerformance"

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

        private val instance = FirebasePerformance()

        @JvmStatic
        public actual fun getInstance(): FirebasePerformance = instance

        @JvmStatic
        public actual fun startTrace(traceName: String): Trace = Trace(traceName).apply { start() }
    }
}
