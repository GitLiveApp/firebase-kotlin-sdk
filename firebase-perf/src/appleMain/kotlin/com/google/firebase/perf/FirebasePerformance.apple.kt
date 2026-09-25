/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import cocoapods.FirebasePerformance.FIRPerformance
import com.google.firebase.perf.metrics.Trace

/** @property ios The underlying Firebase iOS SDK object. */
public actual class FirebasePerformance internal constructor(public val ios: FIRPerformance) {
    public actual var isPerformanceCollectionEnabled: Boolean
        get() = ios.isDataCollectionEnabled()
        set(value) {
            ios.dataCollectionEnabled = value
        }

    public actual fun newTrace(traceName: String): Trace = Trace(ios.traceWithName(traceName))

    override fun equals(other: Any?): Boolean = other is FirebasePerformance && other.ios == ios

    override fun hashCode(): Int = ios.hashCode()

    override fun toString(): String = "FirebasePerformance($ios)"

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

        public actual fun getInstance(): FirebasePerformance = FirebasePerformance(FIRPerformance.sharedInstance())

        public actual fun startTrace(traceName: String): Trace = Trace(FIRPerformance.startTraceWithName(traceName))
    }
}
