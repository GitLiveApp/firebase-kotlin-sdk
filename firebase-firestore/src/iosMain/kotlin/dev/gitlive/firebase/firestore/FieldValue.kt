package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.FIRFieldValue
import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase FieldValue. */
private typealias NativeFieldValue = FIRFieldValue

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
        actual val serverTimestamp: FieldValue get() = FieldValue(NativeFieldValue.fieldValueForServerTimestamp())
        actual val delete: FieldValue get() = FieldValue(NativeFieldValue.fieldValueForDelete())
        actual fun increment(value: Int): FieldValue = FieldValue(NativeFieldValue.fieldValueForIntegerIncrement(value.toLong()))
        actual fun arrayUnion(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.fieldValueForArrayUnion(elements.asList()))
        actual fun arrayRemove(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.fieldValueForArrayRemove(elements.asList()))
    }
}
