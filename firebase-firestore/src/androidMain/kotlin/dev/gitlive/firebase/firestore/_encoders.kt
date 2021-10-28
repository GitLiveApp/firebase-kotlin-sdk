package dev.gitlive.firebase.firestore

import com.google.firebase.firestore.FieldValue

actual fun isFieldValue(value: Any) : Boolean = value is FieldValue
