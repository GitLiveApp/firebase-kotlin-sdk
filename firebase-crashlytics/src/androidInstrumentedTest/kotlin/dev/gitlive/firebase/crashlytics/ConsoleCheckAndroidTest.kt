/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.crashlytics

import androidx.test.platform.app.InstrumentationRegistry
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.Date
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class ConsoleCheckAndroidException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/** Records the same two non-fatals as the iOS ConsoleCheckTest through the Android SDK, so the console shows its real per-event keys. */
class ConsoleCheckAndroidTest {

    @Test
    fun recordNonFatalsForTheConsole() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        fun dump(label: String) {
            val dir = context.filesDir
            val entries = dir.walkTopDown().filter { it != dir }.map { (if (it.isDirectory) "[dir] " else "") + it.relativeTo(dir).path + (if (it.isFile) " (" + it.length() + " bytes)" else "") }.toList()
            println("console-check: $label: ${entries.filter { it.contains("crashlytics", ignoreCase = true) || !it.contains("/") }}")
        }
        // The Crashlytics SDK logs at debug level only when its internal Logger is set to, which Log.isLoggable cannot do from a test.
        runCatching {
            val loggerClass = Class.forName("com.google.firebase.crashlytics.internal.Logger")
            val logger = loggerClass.getMethod("getLogger").invoke(null)
            loggerClass.getDeclaredField("logLevel").apply { isAccessible = true }.setInt(logger, android.util.Log.VERBOSE)
            println("console-check: Crashlytics debug logging enabled")
        }.onFailure { println("console-check: could not enable Crashlytics debug logging: $it") }
        dump("before")
        val options = FirebaseOptions(
            applicationId = "1:846484016111:android:b741561c813fa2678c841a",
            apiKey = "AIzaSyB7pZ7tXymW9WC_ozAppbEs9WBffSmfX9c",
            databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
            storageBucket = "fir-kotlin-sdk.appspot.com",
            projectId = "fir-kotlin-sdk",
            gcmSenderId = "846484016111",
        )
        val app = Firebase.apps(context).firstOrNull() ?: Firebase.initialize(context, options)
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
        dump("after recording")

        // The managed device reinstalls the test APK for every Gradle invocation, so the "next launch" that finalizes
        // and uploads the session is simulated here: a new app instance initializes a new Crashlytics, which closes the
        // previous instance's session and sends it.
        app.delete()
        val relaunched = Firebase.initialize(context, options)
        val crashlytics2 = Firebase.crashlytics(relaunched)
        delay(10.seconds)
        dump("after relaunch")
        crashlytics2.sendUnsentReports()
        delay(30.seconds)
        dump("after upload")
        println("console-check: done")
    }
}
