/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.crashlytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestResult
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse

expect val emulatorHost: String
expect val context: Any
expect fun runTest(test: suspend CoroutineScope.() -> Unit): TestResult
expect annotation class IgnoreForAndroidUnitTest()

@IgnoreForAndroidUnitTest
class FirebaseCrashlyticsTest {

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
    fun testRecordException() = runTest {

        val crashlytics = Firebase.crashlytics
        crashlytics.recordException(Exception("Test Exception"))
    }

    @Test
    fun testLog() = runTest {

        val crashlytics = Firebase.crashlytics
        crashlytics.log("Test Log")
    }

    @Test
    fun testSetUserId() = runTest {

        val crashlytics = Firebase.crashlytics
        crashlytics.setUserId("Test User Id")

    }

    @Test
    fun testSendUnsentReports() = runTest {

        val crashlytics = Firebase.crashlytics
        crashlytics.sendUnsentReports()

    }

    @Test
    fun testDeleteUnsentReports() = runTest {

        val crashlytics = Firebase.crashlytics
        crashlytics.deleteUnsentReports()

    }

    @Test
    fun testDidCrashOnPreviousExecution() = runTest {

        val crashlytics = Firebase.crashlytics
        val didCrash = crashlytics.didCrashOnPreviousExecution()
        assertFalse { didCrash }
    }

    @Test
    fun testSetCrashlyticsCollectionEnabled() = runTest {

        val crashlytics = Firebase.crashlytics
        crashlytics.setCrashlyticsCollectionEnabled(true)

    }
}
