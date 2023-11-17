package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.*
import dev.gitlive.firebase.perf.FirebasePerformance
import dev.gitlive.firebase.perf.IgnoreForAndroidUnitTest
import dev.gitlive.firebase.perf.context
import dev.gitlive.firebase.perf.performance
import kotlinx.coroutines.delay
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@IgnoreForAndroidUnitTest
class TraceTest {

    private lateinit var performance: FirebasePerformance

    @BeforeTest
    fun initializeFirebase() {
        val app = Firebase.apps(context).firstOrNull() ?: Firebase.initialize(
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

        performance = Firebase.performance(app)
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        // Performance runs installation in the background, which crashes if the app is deleted before completion
        delay(1.seconds)
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun testGetLongMetric() = runTest {
        val trace = performance.newTrace("testGetLongMetric")
        trace.start()
        trace.putMetric("Get Long Metric Test", 1L)

        assertEquals(1L,  trace.getLongMetric("Get Long Metric Test"))
        trace.stop()
    }

    @Test
    fun testIncrementMetric() = runTest {
        val trace = performance.newTrace("testIncrementMetric")
        trace.start()
        trace.putMetric("Get Increment Metric Test", 1L)

        trace.incrementMetric("Get Increment Metric Test", 1L)

        assertEquals(2L,  trace.getLongMetric("Get Increment Metric Test"))
        trace.stop()
    }

    @Test
    fun testPutMetric() = runTest {
        val trace = performance.newTrace("testPutMetric")
        trace.start()
        trace.putMetric("Get Put Metric Test", 1L)

        assertEquals(1L,  trace.getLongMetric("Get Put Metric Test"))
        trace.stop()
    }
}