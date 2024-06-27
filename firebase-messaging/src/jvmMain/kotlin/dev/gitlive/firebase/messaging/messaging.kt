@file:JvmName("android")

package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase

public actual val Firebase.messaging: FirebaseMessaging
    get() = TODO("Not yet implemented")

public actual class FirebaseMessaging {
    public actual fun subscribeToTopic(topic: String) {
        TODO("Not yet implemented")
    }

    public actual fun unsubscribeFromTopic(topic: String) {
        TODO("Not yet implemented")
    }

    public actual suspend fun getToken(): String {
        TODO("Not yet implemented")
    }
}
