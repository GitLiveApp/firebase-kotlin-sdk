package dev.gitlive.firebase.firestore

/**
 * A wrapper object for [DocumentReference] which allows to store a reference value and
 * perform a field deletion using the same field.
 */
@Deprecated("Consider using DocumentReference instead")
sealed class FirebaseReference {
    data class Value(val value: DocumentReference) : FirebaseReference()
    object ServerDelete : FirebaseReference()
}

val FirebaseReference.reference: DocumentReference? get() = when (this) {
    is FirebaseReference.Value -> value
    is FirebaseReference.ServerDelete -> null
}
