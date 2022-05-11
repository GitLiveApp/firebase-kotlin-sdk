package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.firebase

actual val DocumentReference.platformValue: Any get() = js
actual fun DocumentReference.Companion.fromPlatformValue(platformValue: Any): DocumentReference =
    DocumentReference(platformValue as firebase.firestore.DocumentReference)
