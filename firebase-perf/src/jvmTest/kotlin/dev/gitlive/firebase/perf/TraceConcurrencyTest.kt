/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.perf

import com.google.firebase.perf.FirebasePerformance
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

class TraceConcurrencyTest {

    /** The in-memory JVM trace increments atomically, as the Android SDK's does. */
    @Test
    fun incrementsFromSeveralThreads() {
        val trace = FirebasePerformance.getInstance().newTrace("concurrent").apply { start() }
        val threads = List(8) { thread { repeat(1_000) { trace.incrementMetric("hits", 1) } } }
        threads.forEach { it.join() }
        trace.stop()
        assertEquals(8_000L, trace.getLongMetric("hits"))
    }
}
