package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestore.FIRFieldValue

actual fun isSpecialValue(value: Any) = when(value) {
    is FIRFieldValue,
    is PlatformGeoPoint,
    is PlatformTimestamp,
    is PlatformDocumentReference -> true
    else -> false
}
