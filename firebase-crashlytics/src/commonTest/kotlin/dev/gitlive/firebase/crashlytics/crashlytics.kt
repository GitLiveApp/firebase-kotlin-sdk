/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.crashlytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.delay
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.seconds

expect val context: Any
expect annotation class IgnoreForAndroidUnitTest()

@IgnoreForAndroidUnitTest
class FirebaseCrashlyticsTest {

    lateinit var crashlytics: FirebaseCrashlytics

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

        crashlytics = Firebase.crashlytics(app)
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun testRecordException() = runTest {
        crashlytics.recordException(Exception("Test Exception"))

        // Delay to ensure Crashlytics completes
        delay(1.seconds)
    }

    @Test
    fun testLog() = runTest {
        crashlytics.log("Test Log")

        // Delay to ensure Crashlytics completes
        delay(1.seconds)
    }

    @Test
    fun testSetUserId() = runTest {
        crashlytics.setUserId("Test User Id")

        // Delay to ensure Crashlytics completes
        delay(1.seconds)
    }

    @Test
    fun testSendUnsentReports() = runTest {
        crashlytics.sendUnsentReports()

        // Delay to ensure Crashlytics completes
        delay(1.seconds)
    }

    @Test
    fun testDeleteUnsentReports() = runTest {
        crashlytics.deleteUnsentReports()

        // Delay to ensure Crashlytics completes
        delay(1.seconds)
    }

    @Test
    fun testDidCrashOnPreviousExecution() = runTest {
        val didCrash = crashlytics.didCrashOnPreviousExecution()
        assertFalse { didCrash }

        // Delay to ensure Crashlytics completes
        delay(1.seconds)
    }

    @Test
    fun testSetCrashlyticsCollectionEnabled() = runTest {
        crashlytics.setCrashlyticsCollectionEnabled(true)

        // Delay to ensure Crashlytics completes
        delay(1.seconds)
    }
}
