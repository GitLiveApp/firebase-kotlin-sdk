package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp

/** Returns the [FirebaseMessaging] instance of the default [FirebaseApp]. */
expect val Firebase.messaging: FirebaseMessaging

/**
 * Top level [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/)
 * singleton that provides methods for subscribing to topics and sending upstream messages.
 */
expect class FirebaseMessaging
