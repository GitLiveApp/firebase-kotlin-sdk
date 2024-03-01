package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** Represents a Firebase FieldValue. */
@Serializable(with = FieldValueSerializer::class)
expect class FieldValue internal constructor(nativeValue: Any) {
    internal val nativeValue: Any

    companion object {
        val serverTimestamp: FieldValue
        val delete: FieldValue
        fun increment(value: Int): FieldValue
        fun arrayUnion(vararg elements: Any): FieldValue
        fun arrayRemove(vararg elements: Any): FieldValue
    }
}
