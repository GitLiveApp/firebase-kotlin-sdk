/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.crashlytics

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.CustomKeysAndValues
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.crashlytics.recordException
import com.google.firebase.crashlytics.setCustomKeys
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Exercises the `com.google.firebase.crashlytics` layer exactly as Android app code would (static accessor, builders,
 * the Kotlin extension DSL, Task API), on every platform.
 */
@IgnoreForAndroidUnitTest
class AndroidSdkSourceCompatTest {

    @BeforeTest
    fun initializeFirebase() {
        if (dev.gitlive.firebase.Firebase.apps(context).isEmpty()) {
            dev.gitlive.firebase.Firebase.initialize(
                context,
                dev.gitlive.firebase.FirebaseOptions(
                    applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                    apiKey = "AIzaSyB7pZ7tXymW9WC_ozAppbEs9WBffSmfX9c",
                    databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                    storageBucket = "fir-kotlin-sdk.appspot.com",
                    projectId = "fir-kotlin-sdk",
                    gcmSenderId = "846484016111",
                ),
            )
        }
    }

    @Test
    fun testAnnotateReports() = runTest {
        val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
        assertEquals(crashlytics, Firebase.crashlytics)

        crashlytics.log("Test log")
        crashlytics.setUserId("Test User Id")
        crashlytics.setCustomKey("string", "value")
        crashlytics.setCustomKey("boolean", true)
        crashlytics.setCustomKey("double", 1.5)
        crashlytics.setCustomKey("float", 2.5f)
        crashlytics.setCustomKey("int", 3)
        crashlytics.setCustomKey("long", 4L)
        crashlytics.setCustomKeys(
            CustomKeysAndValues.Builder()
                .putString("string", "value")
                .putBoolean("boolean", false)
                .putDouble("double", 1.5)
                .putFloat("float", 2.5f)
                .putInt("int", 3)
                .putLong("long", 4L)
                .build(),
        )
        crashlytics.setCustomKeys {
            key("dsl", "value")
            key("count", 5)
        }
        crashlytics.recordException(Exception("Test Exception"))
        crashlytics.recordException(Exception("Test Exception"), CustomKeysAndValues.Builder().putString("source", "builder").build())
        crashlytics.recordException(Exception("Test Exception")) {
            key("source", "dsl")
            key("attempt", 1L)
        }

        // Give the SDK time to persist the events before the test process exits.
        delay(1.seconds)
    }

    @Test
    fun testReportManagement() = runTest {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(true)
        assertTrue(crashlytics.isCrashlyticsCollectionEnabled)
        crashlytics.setCrashlyticsCollectionEnabled(null)
        assertFalse(crashlytics.didCrashOnPreviousExecution())
        // With collection enabled there is nothing pending: the task completes with false on every platform.
        assertFalse(crashlytics.checkForUnsentReports().await())
        crashlytics.sendUnsentReports()
        crashlytics.deleteUnsentReports()

        delay(1.seconds)
    }
}
