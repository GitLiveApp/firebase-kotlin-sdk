package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** Represents a platform specific Firebase FieldValue. */
typealias NativeFieldValue = com.google.firebase.firestore.FieldValue

/** Represents a Firebase FieldValue. */
@Serializable(with = FieldValueSerializer::class)
actual class FieldValue internal actual constructor(internal actual val nativeValue: Any) {
    init {
        require(nativeValue is NativeFieldValue)
    }
    override fun equals(other: Any?): Boolean =
        this === other || other is FieldValue && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    actual companion object {
        actual val serverTimestamp: FieldValue get() = FieldValue(NativeFieldValue.serverTimestamp())
        actual val delete: FieldValue get() = FieldValue(NativeFieldValue.delete())
        actual fun increment(value: Int): FieldValue = FieldValue(NativeFieldValue.increment(value.toDouble()))
        actual fun arrayUnion(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.arrayUnion(*elements))
        actual fun arrayRemove(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.arrayRemove(*elements))
    }
}
