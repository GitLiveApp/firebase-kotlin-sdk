package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.FIRFieldValue

@PublishedApi
internal actual fun isSpecialValue(value: Any): Boolean = when (value) {
    is FIRFieldValue,
    is NativeGeoPoint,
    is NativeTimestamp,
    is NativeDocumentReferenceType,
    -> true
    else -> false
}
