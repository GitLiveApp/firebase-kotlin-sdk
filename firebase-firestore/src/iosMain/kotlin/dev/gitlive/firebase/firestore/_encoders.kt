package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestore.FIRFieldValue

actual fun isFieldValue(value: Any) : Boolean = value is FIRFieldValue
