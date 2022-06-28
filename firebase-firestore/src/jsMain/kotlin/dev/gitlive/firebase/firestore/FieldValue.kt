package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.firebase
import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase FieldValue. */
private typealias NativeFieldValue = firebase.firestore.FieldValue

/** A class representing a Firebase FieldValue. */
@Serializable(with = FieldValueSerializer::class)
actual class FieldValue internal actual constructor(internal actual val nativeValue: Any) {
    init {
        require(nativeValue is NativeFieldValue)
    }
    override fun equals(other: Any?): Boolean =
        this === other || other is FieldValue &&
                (nativeValue as NativeFieldValue).isEqual(other.nativeValue as NativeFieldValue)
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    actual companion object {
        actual val delete: FieldValue get() = FieldValue(NativeFieldValue.delete())
        actual val serverTimestamp: FieldValue get() = FieldValue(NativeFieldValue.serverTimestamp())
        actual fun arrayUnion(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.arrayUnion(*elements))
        actual fun arrayRemove(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.arrayRemove(*elements))
    }
}
