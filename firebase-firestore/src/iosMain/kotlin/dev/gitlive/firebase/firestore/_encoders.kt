package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestore.*

actual fun isSpecialValue(value: Any) : Boolean = value is FIRFieldValue
