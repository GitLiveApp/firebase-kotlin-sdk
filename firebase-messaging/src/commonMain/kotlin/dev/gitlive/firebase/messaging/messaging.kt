package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp

/** Returns the [FirebaseMessaging] instance of the default [FirebaseApp]. */
expect val Firebase.messaging: FirebaseMessaging

expect class FirebaseMessaging
