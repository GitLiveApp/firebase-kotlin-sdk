package dev.gitlive.firebase.messaging

import cocoapods.FirebaseMessaging.FIRMessaging
import dev.gitlive.firebase.Firebase

actual val Firebase.messaging: FirebaseMessaging
    get() = FirebaseMessaging(FIRMessaging.messaging())

actual class FirebaseMessaging(val ios: FIRMessaging) {

}