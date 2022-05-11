package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestore.FIRDocumentReference

actual val DocumentReference.platformValue: Any get() = ios
actual fun DocumentReference.Companion.fromPlatformValue(platformValue: Any): DocumentReference =
    DocumentReference(platformValue as FIRDocumentReference)
