package dev.gitlive.firebase.firestore

import swiftPMImport.dev.gitlive.firebase.firestore.FIRFieldValue

@PublishedApi
internal actual fun isSpecialValue(value: Any): Boolean = when (value) {
    is FIRFieldValue,
    is NativeGeoPoint,
    is NativeTimestamp,
    is NativeDocumentReferenceType,
    -> true
    else -> false
}
