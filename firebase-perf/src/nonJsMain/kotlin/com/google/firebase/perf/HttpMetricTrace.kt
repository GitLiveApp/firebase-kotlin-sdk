/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.perf

import com.google.firebase.perf.metrics.HttpMetric

// The HttpMetric form of the Android SDK's PerformanceKt.trace; the Trace forms are in Performance.kt.

/** Runs [block] between [HttpMetric.start] and [HttpMetric.stop] of this metric. */
public inline fun HttpMetric.trace(block: HttpMetric.() -> Unit) {
    start()
    try {
        block()
    } finally {
        stop()
    }
}
