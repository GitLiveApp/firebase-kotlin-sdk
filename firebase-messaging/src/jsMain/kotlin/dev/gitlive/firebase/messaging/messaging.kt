package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.externals.Messaging
import dev.gitlive.firebase.messaging.externals.getMessaging

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(getMessaging())

actual class FirebaseMessaging(val js: Messaging) {

}