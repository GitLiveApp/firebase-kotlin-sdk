// KMP: this file moves to commonMain; only the timeout changes.
package dev.gitlive.notes.shared

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit // KMP: kotlin.time.Duration.Companion.seconds

/** The app's Cloud Functions; `echo` returns what it is sent. */
class NotesBackend(private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()) {

    suspend fun echo(text: String): String {
        val callable = functions.getHttpsCallable("echo")
        callable.setTimeout(30, TimeUnit.SECONDS) // KMP: callable.setTimeout(30.seconds), from com.google.firebase.functions
        val result = callable.call(mapOf("text" to text)).await()
        return result.data.toString()
    }
}
