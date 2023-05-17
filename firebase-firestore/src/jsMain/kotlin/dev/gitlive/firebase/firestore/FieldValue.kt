package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.firebase
import kotlinx.serialization.Serializable

/** Represents a platform specific Firebase FieldValue. */
private typealias NativeFieldValue = firebase.firestore.FieldValue

/** Represents a Firebase FieldValue. */
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
        actual val serverTimestamp: FieldValue get() = rethrow { FieldValue(NativeFieldValue.serverTimestamp()) }
        actual val delete: FieldValue get() = rethrow { FieldValue(NativeFieldValue.delete()) }
        actual fun increment(value: Int): FieldValue = rethrow { FieldValue(firebase.firestore.FieldValue.increment(value)) }
        actual fun arrayUnion(vararg elements: Any): FieldValue = rethrow { FieldValue(NativeFieldValue.arrayUnion(*elements)) }
        actual fun arrayRemove(vararg elements: Any): FieldValue = rethrow { FieldValue(NativeFieldValue.arrayRemove(*elements)) }
    }
}
