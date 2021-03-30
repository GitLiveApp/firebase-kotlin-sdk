package dev.gitlive.firebase.firestore

actual class FirebaseDocumentReferenceEncoder actual constructor() {
    actual fun encode(value: DocumentReference): Any {
        return value.android
    }
}
