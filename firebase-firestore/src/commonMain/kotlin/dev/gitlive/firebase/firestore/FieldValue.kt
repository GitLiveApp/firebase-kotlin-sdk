package dev.gitlive.firebase.firestore

import com.google.firebase.firestore.fieldValueArrayRemove
import com.google.firebase.firestore.fieldValueArrayUnion
import com.google.firebase.firestore.fieldValueDelete
import com.google.firebase.firestore.fieldValueIncrement
import com.google.firebase.firestore.fieldValueServerTimestamp
import kotlinx.serialization.Serializable

/** Represents a platform specific Firebase FieldValue; the Android-SDK-shaped [com.google.firebase.firestore.FieldValue]. */
public typealias NativeFieldValue = com.google.firebase.firestore.FieldValue

/** Represents a Firebase FieldValue. */
@Serializable(with = FieldValueSerializer::class)
public class FieldValue internal constructor(internal val nativeValue: Any) {
    init {
        require(nativeValue is NativeFieldValue)
    }

    /** The Android-SDK-shaped [com.google.firebase.firestore.FieldValue] this wraps. */
    public val compat: NativeFieldValue get() = nativeValue as NativeFieldValue

    override fun equals(other: Any?): Boolean = this === other || other is FieldValue && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    public companion object {
        public val serverTimestamp: FieldValue get() = FieldValue(fieldValueServerTimestamp())
        public val delete: FieldValue get() = FieldValue(fieldValueDelete())
        public fun increment(value: Int): FieldValue = FieldValue(fieldValueIncrement(value.toLong()))
        public fun increment(value: Double): FieldValue = FieldValue(fieldValueIncrement(value))
        public fun arrayUnion(vararg elements: Any): FieldValue = FieldValue(fieldValueArrayUnion(*elements))
        public fun arrayRemove(vararg elements: Any): FieldValue = FieldValue(fieldValueArrayRemove(*elements))
    }
}
