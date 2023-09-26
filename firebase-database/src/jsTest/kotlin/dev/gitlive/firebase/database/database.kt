package dev.gitlive.firebase.database

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.externals.getDatabase
import dev.gitlive.firebase.database.externals.goOffline
import dev.gitlive.firebase.database.externals.goOnline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

actual val emulatorHost: String = "127.0.0.1" // in JS tests connection is refused if we use localhost

actual val context: Any = Unit
actual fun runTest(test: suspend () -> Unit) = kotlinx.coroutines.test.runTest(timeout = 5.minutes) {
    // in JS tests we need to wait for the database to be connected
    awaitDatabaseConnection()
    test()
}

private suspend fun awaitDatabaseConnection() = withContext(Dispatchers.Default) {
    withTimeout(2.minutes) {
        Firebase.database.reference(".info/connected").valueEvents.first { it.value() }
    }
}
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest
