package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** Represents a Firebase FieldValue. */
@Serializable(with = FieldValueSerializer::class)
public expect class FieldValue internal constructor(nativeValue: Any) {
    internal val nativeValue: Any

    public companion object {
        public val serverTimestamp: FieldValue
        public val delete: FieldValue
        public fun increment(value: Int): FieldValue
        public fun arrayUnion(vararg elements: Any): FieldValue
        public fun arrayRemove(vararg elements: Any): FieldValue
    }
}
