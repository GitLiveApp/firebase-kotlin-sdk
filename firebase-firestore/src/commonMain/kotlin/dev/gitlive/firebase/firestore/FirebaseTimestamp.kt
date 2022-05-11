package dev.gitlive.firebase.firestore

/**
 * A wrapper object for [Timestamp] which allows to store a timestamp value, set a server timestamp and
 * perform a field deletion using the same field.
 */
sealed class FirebaseTimestamp {
    data class Value(val value: Timestamp) : FirebaseTimestamp()
    object ServerValue : FirebaseTimestamp()
    @Deprecated("Consider using DocumentReference.update with FieldValue.delete")
    object ServerDelete : FirebaseTimestamp()
}

val FirebaseTimestamp.timestamp: Timestamp? get() = when (this) {
    is FirebaseTimestamp.Value -> value
    is FirebaseTimestamp.ServerValue,
    is FirebaseTimestamp.ServerDelete -> null
}
