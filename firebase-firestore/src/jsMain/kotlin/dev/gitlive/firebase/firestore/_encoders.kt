package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.firebase

actual fun isSpecialValue(value: Any) = when(value) {
    is firebase.firestore.FieldValue,
    is PlatformGeoPoint,
    is firebase.firestore.Timestamp,
    is firebase.firestore.DocumentReference -> true
    else -> false
}
