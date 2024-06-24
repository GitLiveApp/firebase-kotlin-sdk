package dev.gitlive.firebase.firestore

@PublishedApi
internal actual fun isSpecialValue(value: Any): Boolean = when (value) {
    is NativeFieldValue,
    is NativeGeoPoint,
    is NativeTimestamp,
    is NativeDocumentReferenceType,
    -> true
    else -> false
}
