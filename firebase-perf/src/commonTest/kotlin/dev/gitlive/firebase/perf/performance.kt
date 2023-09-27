/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.perf

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.firebaseOptions
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestResult
import kotlin.test.*

expect val emulatorHost: String
expect val context: Any
expect annotation class IgnoreForAndroidUnitTest()

@IgnoreForAndroidUnitTest
class FirebasePerformanceTest {

    lateinit var performance: FirebasePerformance

    @BeforeTest
    fun initializeFirebase() {
        val app = Firebase.apps(context).firstOrNull() ?: Firebase.initialize(
                    context,
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
    fun testNewTrace() = runTest {

        val trace = performance.newTrace("Test Trace")

        assertNotNull(trace)
    }

    @Test
    fun testPerformanceCollectionEnabled() = runTest {

        performance.setPerformanceCollectionEnabled(false)

        assertFalse(performance.isPerformanceCollectionEnabled())

        performance.setPerformanceCollectionEnabled(true)

        assertTrue(performance.isPerformanceCollectionEnabled())
    }
}
