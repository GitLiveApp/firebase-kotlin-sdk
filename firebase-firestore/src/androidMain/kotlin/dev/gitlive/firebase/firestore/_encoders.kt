package dev.gitlive.firebase.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

actual fun isSpecialValue(value: Any) = when(value) {
    is FieldValue,
    is PlatformGeoPoint,
    is Timestamp,
    is DocumentReference -> true
    else -> false
}
