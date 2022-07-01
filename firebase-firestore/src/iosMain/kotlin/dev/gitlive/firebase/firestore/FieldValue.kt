package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestore.FIRFieldValue
import kotlin.native.concurrent.freeze
import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase FieldValue. */
private typealias NativeFieldValue = FIRFieldValue

/** A class representing a Firebase FieldValue. */
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
        actual val delete: FieldValue get() = FieldValue(NativeFieldValue.fieldValueForDelete())
        actual val serverTimestamp: FieldValue get() = FieldValue(NativeFieldValue.fieldValueForServerTimestamp())
        actual fun arrayUnion(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.fieldValueForArrayUnion(elements.asList().freeze()))
        actual fun arrayRemove(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.fieldValueForArrayRemove(elements.asList().freeze()))
    }
}
