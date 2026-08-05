package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.perf.FirebasePerformance
import dev.gitlive.firebase.perf.IgnoreForAndroidUnitTest
import dev.gitlive.firebase.perf.context
import dev.gitlive.firebase.perf.performance
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.delay
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@IgnoreForAndroidUnitTest
class TraceTest {

    private lateinit var performance: FirebasePerformance
    private lateinit var app: FirebaseApp

    companion object {
        // A fresh instance of the test class is created per test, so the counter has
        // to live here for the generated app names to stay unique across the run.
        private var nextAppId = 0
    }

    @BeforeTest
    fun initializeFirebase() {
        app = Firebase.initialize(
            context,
            FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
            "traceTest${nextAppId++}",
        )

        performance = Firebase.performance(app)
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        app.delete()
    }

    @Test
    fun testGetLongMetric() = runTest {
        val trace = performance.newTrace("testGetLongMetric")
        trace.start()
        trace.putMetric("Get Long Metric Test", 1L)

        assertEquals(1L, trace.getLongMetric("Get Long Metric Test"))
        trace.stop()
    }

    @Test
    fun testIncrementMetric() = runTest {
        val trace = performance.newTrace("testIncrementMetric")
        trace.start()
        trace.putMetric("Get Increment Metric Test", 1L)

        trace.incrementMetric("Get Increment Metric Test", 1L)

        assertEquals(2L, trace.getLongMetric("Get Increment Metric Test"))
        trace.stop()
    }

    @Test
    fun testPutMetric() = runTest {
        val trace = performance.newTrace("testPutMetric")
        trace.start()
        trace.putMetric("Get Put Metric Test", 1L)

        assertEquals(1L, trace.getLongMetric("Get Put Metric Test"))
        trace.stop()
    }

    @Test
    fun testAttributes() = runTest {
        val trace = performance.newTrace("testAttributes")
        trace.start()

        trace.putAttribute("first_attribute", "first_value")
        trace.putAttribute("second_attribute", "second_value")

        assertEquals("first_value", trace.getAttribute("first_attribute"))
        assertEquals(
            mapOf(
                "first_attribute" to "first_value",
                "second_attribute" to "second_value",
            ),
            trace.getAttributes(),
        )

        trace.removeAttribute("first_attribute")

        assertEquals(null, trace.getAttribute("first_attribute"))
        assertEquals(mapOf("second_attribute" to "second_value"), trace.getAttributes())
        trace.stop()
    }
}
