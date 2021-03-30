package dev.gitlive.firebase.firestore

sealed class FirebaseReference {
    data class Value(val value: DocumentReference) : FirebaseReference()
    object ServerDelete : FirebaseReference()
}

val FirebaseReference.reference: DocumentReference? get() = when (this) {
    is FirebaseReference.Value -> value
    is FirebaseReference.ServerDelete -> null
}
