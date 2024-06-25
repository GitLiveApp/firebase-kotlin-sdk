@file:JvmName("android")
package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase

actual val Firebase.messaging: FirebaseMessaging
    get() = TODO("Not yet implemented")

actual class FirebaseMessaging {
    actual fun subscribeToTopic(topic: String) {
        TODO("Not yet implemented")
    }

    actual fun unsubscribeFromTopic(topic: String) {
        TODO("Not yet implemented")
    }

    actual suspend fun getToken(): String {
        TODO("Not yet implemented")
    }
}