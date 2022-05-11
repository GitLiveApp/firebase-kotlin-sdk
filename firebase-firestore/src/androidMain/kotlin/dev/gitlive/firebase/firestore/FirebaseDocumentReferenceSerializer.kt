package dev.gitlive.firebase.firestore

actual val DocumentReference.platformValue: Any get() = android
actual fun DocumentReference.Companion.fromPlatformValue(platformValue: Any): DocumentReference =
    DocumentReference(platformValue as com.google.firebase.firestore.DocumentReference)
