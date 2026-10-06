/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.crashlytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.delay
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

/** A Kotlin exception type of our own, so the console shows how a non-stdlib class name reads as the error domain. */
class ConsoleCheckException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Not a real test: records non-fatals into the fir-kotlin-sdk project so they can be looked at in the Crashlytics
 * console. The iOS SDK only uploads a session's reports on the next launch, so the workflow runs this twice on the same
 * simulator: the first run records, the second run finds the first run's report and uploads it.
 */
class ConsoleCheckTest {

    private val cachesDirectory = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true).first() as String
    private val crashlyticsDirectory = "$cachesDirectory/com.crashlytics.data"

    @Test
    fun recordNonFatalsForTheConsole() = runTest {
        NSUserDefaults.standardUserDefaults.setBool(true, forKey = "/google/firebase/debug_mode")

        println("console-check: bundle identifier = ${NSBundle.mainBundle.bundleIdentifier}")
        println("console-check: arguments = ${NSProcessInfo.processInfo.arguments}")
        println("console-check: HOME = ${NSProcessInfo.processInfo.environment["HOME"]}")
        dump("before initializing Firebase")

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
        // Crashlytics initialises on a background queue after the app is configured; give it a moment.
        delay(5.seconds)
        println("console-check: collection enabled = ${crashlytics.compat.isCrashlyticsCollectionEnabled}")
        println("console-check: did crash on previous execution = ${crashlytics.didCrashOnPreviousExecution()}")

        crashlytics.setUserId("console-check")
        crashlytics.setCustomKey("recorded_at", NSDate().toString())
        crashlytics.log("console-check: recording two non-fatals")
        crashlytics.recordException(ConsoleCheckException("Recorded by ConsoleCheckTest on the iOS simulator", IllegalArgumentException("The cause of the console check")))
        crashlytics.recordException(
            IllegalStateException("A second exception type, to show the grouping by Kotlin class"),
            mapOf("attempt" to 1, "source" to "ConsoleCheckTest"),
        )

        delay(10.seconds)
        dump("after recording")
        delay(60.seconds)
        dump("after waiting for the upload")
    }

    private fun dump(label: String) {
        println("console-check: $crashlyticsDirectory ($label)")
        val paths = NSFileManager.defaultManager.subpathsAtPath(crashlyticsDirectory)
        if (paths == null) {
            println("console-check:   (directory does not exist)")
        } else {
            paths.forEach { println("console-check:   $it") }
        }
    }
}
