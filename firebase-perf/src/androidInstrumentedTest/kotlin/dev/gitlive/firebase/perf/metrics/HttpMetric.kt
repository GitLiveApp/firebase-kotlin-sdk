package dev.gitlive.firebase.perf.metrics

import com.google.firebase.perf.v1.NetworkRequestMetric
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.perf.FirebasePerformance
import dev.gitlive.firebase.perf.context
import dev.gitlive.firebase.perf.performance
import dev.gitlive.firebase.runBlockingTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class AndroidHttpMetricTest {

    private lateinit var performance: FirebasePerformance

    companion object {
        const val URL = "https://example.com"
        val httpMethod = NetworkRequestMetric.HttpMethod.POST.name
    }

    @BeforeTest
    fun initializeFirebase() {
        val app = Firebase.apps(context).firstOrNull() ?: Firebase.initialize(
            context,
            FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
        )

        performance = Firebase.performance(app)
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        // Performance runs installation in the background, which crashes if the app is deleted before completion
        delay(5.seconds)
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun testGetAttributes() = runTest {
        val trace = performance.newHttpMetric(URL, httpMethod)
        trace.start()
        val values = listOf(1, 2, 3)

        values.forEach {
            trace.putAttribute("Test_Get_Attributes_$it", "Test Get Attributes Value $it")
        }

        val attributes = trace.getAttributes()

        assertEquals(3, attributes.size)

        // TODO: refactor? this should check if keys are same as you have placed and if values are same as you have placed
        attributes.onEachIndexed { _, entry ->
            assertEquals(entry.key.last(), entry.value.last())
        }

        trace.stop()
    }

    @Test
    fun testGetAttribute() = runTest {
        val trace = performance.newHttpMetric(URL, httpMethod)
        trace.start()
        trace.putAttribute("Test_Get_Attribute", "Test Get Attribute Value")

        assertEquals("Test Get Attribute Value", trace.getAttribute("Test_Get_Attribute"))
        trace.stop()
    }

    @Test
    fun testPutAttribute() = runTest {
        val trace = performance.newHttpMetric(URL, httpMethod)
        trace.start()
        trace.putAttribute("Test_Put_Attribute", "Test Put Attribute Value")

        assertEquals("Test Put Attribute Value", trace.getAttribute("Test_Put_Attribute"))
        trace.stop()
    }

    @Test
    fun testRemoveAttribute() = runTest {
        val trace = performance.newHttpMetric(URL, httpMethod)
        trace.start()

        trace.putAttribute("Test_Put_Attribute", "Test Put Attribute Value")
        assertEquals("Test Put Attribute Value", trace.getAttribute("Test_Put_Attribute"))

        trace.removeAttribute("Test_Put_Attribute")
        assertEquals(null, trace.getAttribute("Test_Put_Attribute"))

        trace.stop()
    }

    @Test
    fun testSettingHttpMetrics() = runTest {
        val trace = performance.newHttpMetric(URL, httpMethod)
        trace.start()

        trace.setHttpResponseCode(1)
        trace.setRequestPayloadSize(10L)
        trace.setResponseContentType("application/json")
        trace.setResponsePayloadSize(44L)

        trace.stop()
    }
}
