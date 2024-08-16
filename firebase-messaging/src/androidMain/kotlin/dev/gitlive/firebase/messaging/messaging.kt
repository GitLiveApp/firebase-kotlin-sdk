@file:JvmName("android")

package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase
import kotlinx.coroutines.tasks.await

public val FirebaseMessaging.android: com.google.firebase.messaging.FirebaseMessaging get() = com.google.firebase.messaging.FirebaseMessaging.getInstance()

public actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(com.google.firebase.messaging.FirebaseMessaging.getInstance())

public actual class FirebaseMessaging(internal val android: com.google.firebase.messaging.FirebaseMessaging) {
    public actual fun subscribeToTopic(topic: String) {
        android.subscribeToTopic(topic)
    }

    public actual fun unsubscribeFromTopic(topic: String) {
        android.unsubscribeFromTopic(topic)
    }

    public actual suspend fun getToken(): String = android.token.await()

    public actual suspend fun deleteToken() {
        android.deleteToken().await()
    }
}
