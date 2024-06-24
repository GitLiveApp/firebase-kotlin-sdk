package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.externals.Messaging
import dev.gitlive.firebase.messaging.externals.getMessaging
import kotlinx.coroutines.await

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(getMessaging())

actual class FirebaseMessaging(val js: Messaging) {
    actual fun subscribeToTopic(topic: String) {
        // This is not supported in the JS SDK
        // https://firebase.google.com/docs/reference/js/messaging_.md#@firebase/messaging
        throw NotImplementedError("Subscribing to topics is not supported in the JS SDK")
    }

    actual fun unsubscribeFromTopic(topic: String) {
        // This is not supported in the JS SDK
        // https://firebase.google.com/docs/reference/js/messaging_.md#@firebase/messaging
        throw NotImplementedError("Unsubscribing from topics is not supported in the JS SDK")
    }

    actual suspend fun getToken(): String = dev.gitlive.firebase.messaging.externals.getToken(js).await()
}
