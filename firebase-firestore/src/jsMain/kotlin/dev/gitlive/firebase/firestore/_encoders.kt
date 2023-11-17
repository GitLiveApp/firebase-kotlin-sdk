package dev.gitlive.firebase.firestore

@PublishedApi
internal actual fun isSpecialValue(value: Any) = when(value) {
    is NativeFieldValue,
    is NativeGeoPoint,
    is NativeTimestamp,
    is NativeDocumentReference -> true
    else -> false
}