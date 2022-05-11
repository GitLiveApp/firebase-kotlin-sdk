package dev.gitlive.firebase.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint

actual fun isSpecialValue(value: Any) = when(value) {
    is FieldValue,
    is GeoPoint,
    is Timestamp,
    is DocumentReference -> true
    else -> false
}
