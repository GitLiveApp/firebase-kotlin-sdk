package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.firebase

actual fun isSpecialValue(value: Any) = when(value) {
    is firebase.firestore.FieldValue,
    is NativeGeoPoint,
    is NativeTimestamp,
    is NativeDocumentReference -> true
    else -> false
}
