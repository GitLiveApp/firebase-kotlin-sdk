package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.externals.Messaging
import dev.gitlive.firebase.messaging.externals.getMessaging
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(getMessaging())

actual class FirebaseMessaging(val js: Messaging) {
    actual fun subscribeToTopic(topic: String) {
        GlobalScope.launch {
            js.subscribeToTopic(arrayOf(getToken()), topic)
        }
    }

    actual fun unsubscribeFromTopic(topic: String) {
        GlobalScope.launch {
            js.unsubscribeFromTopic(arrayOf(getToken()), topic)
        }
    }

    actual suspend fun getToken(): String = dev.gitlive.firebase.messaging.externals.getToken(js).await()
}