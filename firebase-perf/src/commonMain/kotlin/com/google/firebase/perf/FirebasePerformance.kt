/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import com.google.firebase.perf.metrics.Trace

/**
 * Firebase Performance Monitoring, as the Android SDK's `com.google.firebase.perf.FirebasePerformance`: the entry point
 * for custom traces (and, where the platform supports it, HTTP metrics through `newHttpMetric`).
 */
public expect class FirebasePerformance {
    /**
     * Whether performance monitoring is enabled. The setting is persisted and applied on future runs of the app; it
     * does not affect instrumentation configured at build time.
     */
    public var isPerformanceCollectionEnabled: Boolean

    /**
     * Creates a [Trace] with the given name, which must have no leading or trailing whitespace, no leading underscore
     * and at most [MAX_TRACE_NAME_LENGTH] characters.
     */
    public fun newTrace(traceName: String): Trace

    /** The HTTP method names accepted by `newHttpMetric`; the Android SDK's `@StringDef`. */
    @Retention(AnnotationRetention.SOURCE)
    public annotation class HttpMethod {
        public companion object {
            public val CONNECT: String
            public val DELETE: String
            public val GET: String
            public val HEAD: String
            public val OPTIONS: String
            public val PATCH: String
            public val POST: String
            public val PUT: String
            public val TRACE: String
        }
    }

    public companion object {
        /** Maximum length of a trace name. */
        public val MAX_TRACE_NAME_LENGTH: Int

        /** The [FirebasePerformance] singleton. */
        public fun getInstance(): FirebasePerformance

        /** Creates a [Trace] with the given name and starts it. */
        public fun startTrace(traceName: String): Trace
    }
}
