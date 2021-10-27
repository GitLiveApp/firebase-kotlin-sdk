package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.firebase

actual fun isSpecialValue(value: Any) : Boolean = value is firebase.firestore.FieldValue
