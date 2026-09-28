/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.perf

import com.google.firebase.Firebase
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.HttpMetric
import com.google.firebase.perf.newHttpMetric
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
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

/** The `com.google.firebase.perf.metrics.HttpMetric` part of the layer, which the JS SDK does not have. */
@IgnoreForAndroidUnitTest
class HttpMetricTest {

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
    fun testHttpMetric() = runTest {
        val metric: HttpMetric = Firebase.performance.newHttpMetric("https://fir-kotlin-sdk.firebaseio.com/.json", FirebasePerformance.HttpMethod.GET)
        metric.start()
        metric.putAttribute("first", "one")
        metric.putAttribute("second", "two")
        assertEquals("one", metric.getAttribute("first"))
        assertEquals(mapOf("first" to "one", "second" to "two"), metric.attributes)
        metric.removeAttribute("first")
        assertNull(metric.getAttribute("first"))
        metric.setHttpResponseCode(200)
        metric.setRequestPayloadSize(0)
        metric.setResponsePayloadSize(1024)
        metric.setResponseContentType("application/json")
        metric.stop()
        assertEquals(100, HttpMetric.MAX_ATTRIBUTE_VALUE_LENGTH)
    }

    @Test
    fun testHttpMetricTraceBlock() = runTest {
        var seen: String? = null
        Firebase.performance.newHttpMetric("https://fir-kotlin-sdk.firebaseio.com/.json", FirebasePerformance.HttpMethod.POST).trace {
            putAttribute("key", "value")
            setHttpResponseCode(201)
            seen = getAttribute("key")
        }
        assertEquals("value", seen)
    }
}
