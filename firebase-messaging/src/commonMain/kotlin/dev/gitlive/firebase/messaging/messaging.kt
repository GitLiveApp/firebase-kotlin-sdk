package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp

/** Returns the [FirebaseMessaging] instance of the default [FirebaseApp]. */
expect val Firebase.messaging: FirebaseMessaging

/**
 * Top level [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/)
 * singleton that provides methods for subscribing to topics and sending upstream messages.
 */
expect class FirebaseMessaging {
    /**
     * Subscribe to a topic.
     * @param topic The topic to subscribe to.
     */
    fun subscribeToTopic(topic: String)

    /**
     * Unsubscribe from a topic.
     * @param topic The topic to unsubscribe from.
     */
    fun unsubscribeFromTopic(topic: String)

    /**
     * Get FCM token for client
     * @return [String] FCM token
     */
    suspend fun getToken(): String
}
