package dev.gitlive.firebase.firestore

actual val DocumentReference.platformValue: Any get() = ios
actual fun DocumentReference.Companion.fromPlatformValue(platformValue: Any): DocumentReference =
    DocumentReference(platformValue as FIRDocumentReference)
