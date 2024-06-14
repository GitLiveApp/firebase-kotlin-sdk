@file:JvmName("android")
package dev.gitlive.firebase.messaging

import dev.gitlive.firebase.Firebase

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(com.google.firebase.messaging.FirebaseMessaging.getInstance())

actual class FirebaseMessaging(val android: com.google.firebase.messaging.FirebaseMessaging) {

}