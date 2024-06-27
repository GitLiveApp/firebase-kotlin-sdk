package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.externals.Messaging
import dev.gitlive.firebase.messaging.externals.getMessaging
import kotlinx.coroutines.await

public actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(getMessaging())

public actual class FirebaseMessaging(public val js: Messaging) {
    public actual fun subscribeToTopic(topic: String) {
        // This is not supported in the JS SDK
        // https://firebase.google.com/docs/reference/js/messaging_.md#@firebase/messaging
        throw NotImplementedError("Subscribing to topics is not supported in the JS SDK")
    }

    public actual fun unsubscribeFromTopic(topic: String) {
        // This is not supported in the JS SDK
        // https://firebase.google.com/docs/reference/js/messaging_.md#@firebase/messaging
        throw NotImplementedError("Unsubscribing from topics is not supported in the JS SDK")
    }

    public actual suspend fun getToken(): String = dev.gitlive.firebase.messaging.externals.getToken(js).await()
}
