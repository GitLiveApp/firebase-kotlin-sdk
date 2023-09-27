package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.firebaseOptions
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.perf.FirebasePerformance
import dev.gitlive.firebase.perf.context
import dev.gitlive.firebase.perf.performance
import dev.gitlive.firebase.runBlockingTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class JsTraceTest {

    private lateinit var performance: FirebasePerformance

    @BeforeTest
    fun initializeFirebase() {
        val app = Firebase.apps(context).firstOrNull() ?:Firebase.initialize(
            dev.gitlive.firebase.perf.context,
            firebaseOptions
        )

        performance = Firebase.performance(app)
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun testGetAttribute() {
        val trace = performance.newTrace("testGetAttribute")
        trace.start()
        trace.putAttribute("Test_Get_Attribute", "Test Get Attribute Value")

        assertEquals("Test Get Attribute Value", trace.getAttribute("Test_Get_Attribute"))
        trace.stop()
    }

    @Test
    fun testPutAttribute() {
        val trace = performance.newTrace("testPutAttribute")
        trace.start()
        trace.putAttribute("Test_Put_Attribute", "Test Put Attribute Value")

        assertEquals("Test Put Attribute Value", trace.getAttribute("Test_Put_Attribute"))
        trace.stop()
    }
}