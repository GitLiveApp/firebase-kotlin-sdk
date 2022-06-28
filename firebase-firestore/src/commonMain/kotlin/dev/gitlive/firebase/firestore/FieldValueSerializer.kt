package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseEncoder
import dev.gitlive.firebase.SpecialValueSerializer

/** A serializer for [FieldValue]. Must be used in conjunction with [FirebaseEncoder]. */
object FieldValueSerializer : SpecialValueSerializer<FieldValue>(
    serialName = "FieldValue",
    toNativeValue = FieldValue::nativeValue,
    fromNativeValue = { value -> FieldValue(requireNotNull(value)) }
)
