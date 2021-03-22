package dev.gitlive.firebase.firestore

sealed class FirebaseTimestamp {
    data class Value(val value: Timestamp) : FirebaseTimestamp()
    object ServerValue : FirebaseTimestamp()
}

val FirebaseTimestamp.timestamp: Timestamp? get() = when (this) {
    is FirebaseTimestamp.Value -> value
    is FirebaseTimestamp.ServerValue -> null
}
