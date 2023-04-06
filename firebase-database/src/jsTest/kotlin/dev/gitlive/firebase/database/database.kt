package dev.gitlive.firebase.database

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.externals.getDatabase
import dev.gitlive.firebase.database.externals.goOffline
import dev.gitlive.firebase.database.externals.goOnline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

actual val emulatorHost: String = "127.0.0.1" // in JS connection is refused if we use localhost

actual val context: Any = Unit
actual fun runTest(test: suspend () -> Unit) = kotlinx.coroutines.test.runTest {
    val db = getDatabase()
    goOnline(db)
    awaitDatabaseConnection()
    test()
    // in JS tests are running infinitely without going database offline
    goOffline(db)
}

private suspend fun awaitDatabaseConnection() = withContext(Dispatchers.Default) {
    withTimeout(5.seconds) {
        Firebase.database.reference(".info/connected").valueEvents.first { it.value() }
    }
}
