/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.perf

import dev.gitlive.firebase.*
import kotlinx.coroutines.*
import platform.Foundation.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = runBlocking {
    val testRun = MainScope().async { test() }
    while (testRun.isActive) {
        NSRunLoop.mainRunLoop.runMode(
            NSDefaultRunLoopMode,
            beforeDate = NSDate.create(timeInterval = 1.0, sinceDate = NSDate())
        )
        yield()
    }
    testRun.await()
}

class iOSPerformanceTest {

    @BeforeTest
    fun initializeFirebase() {
        Firebase
            .takeIf { Firebase.apps(context).isEmpty() }
            ?.apply {
                initialize(
                    context,
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
    }

    @Test
    fun testInstrumentationEnabled() {

        val performance = Firebase.performance

        assertTrue(performance.isInstrumentationEnabled())

        performance.setInstrumentationEnabled(false)

        assertFalse(performance.isInstrumentationEnabled())
    }
}
