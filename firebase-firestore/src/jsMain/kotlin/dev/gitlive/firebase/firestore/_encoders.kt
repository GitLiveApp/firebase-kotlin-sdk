package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.firebase

actual fun isSpecialValue(value: Any) = when(value) {
    is firebase.firestore.FieldValue,
    is PlatformGeoPoint,
    is PlatformTimestamp,
    is PlatformDocumentReference -> true
    else -> false
}
