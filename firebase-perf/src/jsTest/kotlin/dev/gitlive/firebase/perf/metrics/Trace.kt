package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.perf.FirebasePerformance
import dev.gitlive.firebase.perf.context
import dev.gitlive.firebase.perf.performance
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class JsTraceTest {

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
            "traceJsTest${nextAppId++}",
        )

        performance = Firebase.performance(app)
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        app.delete()
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
