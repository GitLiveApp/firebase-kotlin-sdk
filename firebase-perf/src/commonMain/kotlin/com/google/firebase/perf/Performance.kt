/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import com.google.firebase.Firebase
import com.google.firebase.perf.metrics.Trace

/*
 * The Kotlin extensions of the Android SDK's firebase-perf (PerformanceKt), as plain common code: on Android the facade
 * is a header stub that is stripped, so the SDK's own facade binds. The HttpMetric form of `trace` is in nonJsMain.
 */

/** The [FirebasePerformance] singleton; the Android SDK's `Firebase.performance`. */
public val Firebase.performance: FirebasePerformance
    get() = FirebasePerformance.getInstance()

/** Runs [block] between [Trace.start] and [Trace.stop] of this trace and returns its result. */
public inline fun <T> Trace.trace(block: Trace.() -> T): T {
    start()
    try {
        return block()
    } finally {
        stop()
    }
}

/** Creates a trace named [name], runs [block] between its start and stop and returns the result. */
public inline fun <T> trace(name: String, block: Trace.() -> T): T = FirebasePerformance.getInstance().newTrace(name).trace(block)
