package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.externals.awaitUnit
import dev.gitlive.firebase.externals.awaitValue
import dev.gitlive.firebase.externals.toKotlinString
import dev.gitlive.firebase.messaging.externals.Messaging
import dev.gitlive.firebase.messaging.externals.getMessaging

public actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(getMessaging())

public val FirebaseMessaging.js: Messaging get() = js

public actual class FirebaseMessaging(internal val js: Messaging) {
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

    public actual suspend fun getToken(): String = dev.gitlive.firebase.messaging.externals.getToken(js).awaitValue().toKotlinString()

    public actual suspend fun deleteToken() {
        dev.gitlive.firebase.messaging.externals.deleteToken(js).awaitUnit()
    }
}
