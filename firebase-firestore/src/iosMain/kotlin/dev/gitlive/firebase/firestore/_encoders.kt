package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestore.FIRDocumentReference
import cocoapods.FirebaseFirestore.FIRFieldValue
import cocoapods.FirebaseFirestore.FIRTimestamp

actual fun isSpecialValue(value: Any) = when(value) {
    is FIRFieldValue,
    is PlatformGeoPoint,
    is FIRTimestamp,
    is FIRDocumentReference -> true
    else -> false
}
