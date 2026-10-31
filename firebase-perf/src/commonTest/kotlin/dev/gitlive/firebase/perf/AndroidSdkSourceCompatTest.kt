/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.perf

import com.google.firebase.Firebase
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.AddTrace
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.perf.performance
import com.google.firebase.perf.trace
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.delay
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Exercises the `com.google.firebase.perf` layer exactly as Android app code would (the singleton, `Firebase.performance`,
 * traces with metrics and attributes, `startTrace`, the `trace { }` blocks, the constants), on every platform.
 */
@IgnoreForAndroidUnitTest
class AndroidSdkSourceCompatTest {

    @BeforeTest
    fun initializeFirebase() {
        dev.gitlive.firebase.Firebase.apps(context).firstOrNull() ?: dev.gitlive.firebase.Firebase.initialize(
            context,
            dev.gitlive.firebase.FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
        )
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        // Performance runs installation in the background, which crashes if the app is deleted before completion
        delay(1.seconds)
        dev.gitlive.firebase.Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun testInstances() {
        val performance: FirebasePerformance = FirebasePerformance.getInstance()
        assertEquals(performance, Firebase.performance)
        assertEquals(performance, dev.gitlive.firebase.Firebase.performance.compat)
    }

    @Test
    fun testCollectionEnabled() = runTest {
        val performance = Firebase.performance
        performance.isPerformanceCollectionEnabled = false
        assertFalse(performance.isPerformanceCollectionEnabled)
        performance.isPerformanceCollectionEnabled = true
        assertTrue(performance.isPerformanceCollectionEnabled)
    }

    @Test
    fun testTraceMetricsAndAttributes() = runTest {
        val trace: Trace = Firebase.performance.newTrace("compat_trace")
        trace.start()
        trace.putMetric("requests", 2)
        trace.incrementMetric("requests", 3)
        assertEquals(5, trace.getLongMetric("requests"))
        assertEquals(0, trace.getLongMetric("missing"))
        trace.putAttribute("first", "one")
        trace.putAttribute("second", "two")
        assertEquals("one", trace.getAttribute("first"))
        assertEquals(mapOf("first" to "one", "second" to "two"), trace.attributes)
        trace.removeAttribute("first")
        assertNull(trace.getAttribute("first"))
        assertEquals(mapOf("second" to "two"), trace.attributes)
        trace.stop()
    }

    @Test
    fun testStartTraceAndTraceBlocks() = runTest {
        val started = FirebasePerformance.startTrace("compat_started")
        started.putMetric("count", 1)
        assertEquals(1, started.getLongMetric("count"))
        started.stop()

        val fromBlock = trace("compat_block") {
            putMetric("count", 7)
            getLongMetric("count")
        }
        assertEquals(7, fromBlock)

        val fromTraceBlock = Firebase.performance.newTrace("compat_trace_block").trace {
            putAttribute("key", "value")
            getAttribute("key")
        }
        assertEquals("value", fromTraceBlock)
    }

    @Test
    fun testConstants() {
        assertEquals(100, FirebasePerformance.MAX_TRACE_NAME_LENGTH)
        assertEquals(40, Trace.MAX_ATTRIBUTE_KEY_LENGTH)
        assertEquals(100, Trace.MAX_ATTRIBUTE_VALUE_LENGTH)
        assertEquals(5, Trace.MAX_TRACE_CUSTOM_ATTRIBUTES)
        assertEquals(100, Trace.MAX_TRACE_NAME_LENGTH)
        assertEquals("GET", FirebasePerformance.HttpMethod.GET)
        assertEquals("POST", FirebasePerformance.HttpMethod.POST)
        assertEquals("compat_annotated", annotated())
    }

    @AddTrace(name = "compat_annotated")
    private fun annotated(): String = "compat_annotated"
}
