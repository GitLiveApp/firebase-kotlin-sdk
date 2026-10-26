/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.crashlytics

import androidx.test.platform.app.InstrumentationRegistry
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ConsoleCheckAndroidException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/** Records the same two non-fatals as the iOS ConsoleCheckTest through the Android SDK, so the console shows its real per-event keys. */
class ConsoleCheckAndroidTest {

    @Test
    fun recordNonFatalsForTheConsole() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val app = Firebase.apps(context).firstOrNull() ?: Firebase.initialize(
            context,
            FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyB7pZ7tXymW9WC_ozAppbEs9WBffSmfX9c",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
        )
        val crashlytics = Firebase.crashlytics(app)
        println("console-check: collection enabled = ${crashlytics.compat.isCrashlyticsCollectionEnabled}")
        println("console-check: did crash on previous execution = ${crashlytics.didCrashOnPreviousExecution()}")
        crashlytics.sendUnsentReports()

        crashlytics.setUserId("console-check-android")
        crashlytics.setCustomKey("recorded_at", Date().toString())
        crashlytics.log("console-check: recording two non-fatals on Android")
        crashlytics.recordException(ConsoleCheckAndroidException("Recorded by ConsoleCheckAndroidTest on the Android emulator", IllegalArgumentException("The cause of the console check")))
        crashlytics.recordException(
            IllegalStateException("Keyed non-fatal: on Android its per-event keys are real custom keys of the event"),
            mapOf("attempt" to 2, "source" to "ConsoleCheckAndroidTest", "requested_at" to Date().toString(), "retries" to 3L, "flag" to true),
        )

        delay(15.seconds)
        crashlytics.sendUnsentReports()
        delay(30.seconds)
        println("console-check: done")
    }
}
