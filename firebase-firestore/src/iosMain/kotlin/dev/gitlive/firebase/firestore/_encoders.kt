package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestore.FIRFieldValue
import cocoapods.FirebaseFirestore.FIRTimestamp

actual fun isSpecialValue(value: Any) = when(value) {
    is FIRFieldValue,
    is PlatformGeoPoint,
    is FIRTimestamp,
    is PlatformDocumentReference -> true
    else -> false
}
