package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** A class representing a Firebase FieldValue. */
@Serializable(with = FieldValueSerializer::class)
expect class FieldValue internal constructor(nativeValue: Any) {
    // implementation note. unfortunately declaring a common `expect PlatformFieldValue`
    // is not possible due to different platform class signatures
    internal val nativeValue: Any

    companion object {
        val delete: FieldValue
        val serverTimestamp: FieldValue
        fun arrayUnion(vararg elements: Any): FieldValue
        fun arrayRemove(vararg elements: Any): FieldValue
    }
}
