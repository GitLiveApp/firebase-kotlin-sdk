// KMP: this file moves to commonMain unchanged; its Android-only half is in platform/MessagingExports.kt.
package dev.gitlive.notes.shared

import com.google.firebase.messaging.FirebaseMessaging
import dev.gitlive.notes.shared.platform.configureMessagingExports
import kotlinx.coroutines.tasks.await

/** Cloud Messaging registration; received messages are handled by [dev.gitlive.notes.shared.platform.NotesMessagingService]. */
class NotesMessaging(private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()) {

    fun enable() {
        messaging.isAutoInitEnabled = true
        configureMessagingExports(messaging)
    }

    // Written as the function call: common code declares getToken() as a function, so the Kotlin synthetic property
    // `messaging.token` an Android app might use instead would not resolve there.
    suspend fun token(): String = messaging.getToken().await()

    suspend fun subscribe(topic: String) {
        messaging.subscribeToTopic(topic).await()
    }
}
