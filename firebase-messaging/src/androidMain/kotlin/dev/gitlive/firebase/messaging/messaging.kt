@file:JvmName("android")
package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase
import kotlinx.coroutines.tasks.await

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(com.google.firebase.messaging.FirebaseMessaging.getInstance())

actual class FirebaseMessaging(val android: com.google.firebase.messaging.FirebaseMessaging) {
    actual fun subscribeToTopic(topic: String) {
        android.subscribeToTopic(topic)
    }

    actual fun unsubscribeFromTopic(topic: String) {
        android.unsubscribeFromTopic(topic)
    }

    actual suspend fun getToken(): String = android.token.await()
}