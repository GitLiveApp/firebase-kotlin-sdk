package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.perf.FirebasePerformance
import dev.gitlive.firebase.perf.performance
import kotlinx.coroutines.CoroutineScope
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

expect val emulatorHost: String
expect val context: Any
expect fun runTest(test: suspend CoroutineScope.() -> Unit)

class TraceTest {

    private lateinit var performance: FirebasePerformance

    private lateinit var trace: Trace

    @BeforeTest
    fun initializeFirebase() {
        Firebase
            .takeIf { Firebase.apps(dev.gitlive.firebase.perf.context).isEmpty() }
            ?.apply {
                initialize(
                    dev.gitlive.firebase.perf.context,
                    FirebaseOptions(
                        applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                        apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                        databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                        storageBucket = "fir-kotlin-sdk.appspot.com",
                        projectId = "fir-kotlin-sdk",
                        gcmSenderId = "846484016111"
                    )
                )
            }

        performance = Firebase.performance

        trace = performance.newTrace("Test Trace")
    }

    @Test
    fun testGetLongMetric() {

        trace.putMetric("Get Long Metric Test", 1L)

        assertEquals(1L,  trace.getLongMetric("Get Long Metric Test"))
    }

    @Test
    fun testIncrementMetric() {

        trace.putMetric("Get Increment Metric Test", 1L)

        trace.incrementMetric("Get Increment Metric Test", 1L)

        assertEquals(2L,  trace.getLongMetric("Get Increment Metric Test"))
    }

    @Test
    fun testPutMetric() {

        trace.putMetric("Get Put Metric Test", 1L)

        assertEquals(1L,  trace.getLongMetric("Get Put Metric Test"))
    }

    @Test
    fun testGetAttributes() {

        val values = listOf(1, 2, 3)

        values.forEach {
            trace.putAttribute("Test Get Attributes Attribute $it", "Test Get Attributes Value $it")
        }

        val expected = values.associateBy({"Test Get Attributes Attribute $it"}, {"Test Get Attributes Value $it"})

        assertEquals(expected, trace.getAttributes())

        values.forEach {
            trace.removeAttribute("Test Get Attributes Attribute $it")
        }
    }

    @Test
    fun testGetAttribute() {

        trace.putAttribute("Test Get Attribute Attribute", "Test Get Attribute Value")

        assertEquals("Test Get Attribute Value", trace.getAttribute("Test Get Attribute Attribute"))
    }
}