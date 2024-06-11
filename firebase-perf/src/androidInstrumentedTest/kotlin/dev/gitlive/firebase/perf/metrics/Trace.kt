package dev.gitlive.firebase.perf.metrics

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

class AndroidTraceTest {

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
        val trace = performance.newTrace("testGetAttributes")
        trace.start()
        val values = listOf(1, 2, 3)

        values.forEach {
            trace.putAttribute("Test_Get_Attributes_$it", "Test Get Attributes Value $it")
        }

        val attributes = trace.getAttributes()

        assertEquals(3, attributes.size)

        attributes.onEachIndexed { _, entry ->

            assertEquals(entry.key.last(), entry.value.last())
        }

        trace.stop()
    }

    @Test
    fun testGetAttribute() = runTest {
        val trace = performance.newTrace("testGetAttribute")
        trace.start()
        trace.putAttribute("Test_Get_Attribute", "Test Get Attribute Value")

        assertEquals("Test Get Attribute Value", trace.getAttribute("Test_Get_Attribute"))
        trace.stop()
    }

    @Test
    fun testPutAttribute() = runTest {
        val trace = performance.newTrace("testPutAttribute")
        trace.start()
        trace.putAttribute("Test_Put_Attribute", "Test Put Attribute Value")

        assertEquals("Test Put Attribute Value", trace.getAttribute("Test_Put_Attribute"))
        trace.stop()
    }
}
