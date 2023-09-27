/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.perf

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.firebaseOptions
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlin.test.*

actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest

class JsPerformanceTest {

    private lateinit var performance: FirebasePerformance

    @BeforeTest
    fun initializeFirebase() {
        val app = Firebase.apps(context).firstOrNull() ?:Firebase.initialize(
            context,
            firebaseOptions
        )

        performance = Firebase.performance(app)
    }

    @Test
    fun testInstrumentationEnabled() = runTest {

        val performance = Firebase.performance

        performance.setInstrumentationEnabled(false)

        assertFalse(performance.isInstrumentationEnabled())

        performance.setInstrumentationEnabled(true)

        assertTrue(performance.isInstrumentationEnabled())
    }
}
