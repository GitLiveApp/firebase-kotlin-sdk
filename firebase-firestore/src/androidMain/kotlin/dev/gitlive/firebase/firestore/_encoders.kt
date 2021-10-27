package dev.gitlive.firebase.firestore

import com.google.firebase.firestore.FieldValue

actual fun isSpecialValue(value: Any) : Boolean = value is FieldValue
