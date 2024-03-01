package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.firestore.externals.deleteField
import kotlinx.serialization.Serializable
import dev.gitlive.firebase.firestore.externals.serverTimestamp as jsServerTimestamp
import dev.gitlive.firebase.firestore.externals.arrayRemove as jsArrayRemove
import dev.gitlive.firebase.firestore.externals.arrayUnion as jsArrayUnion
import dev.gitlive.firebase.firestore.externals.increment as jsIncrement

/** Represents a platform specific Firebase FieldValue. */
typealias NativeFieldValue = dev.gitlive.firebase.firestore.externals.FieldValue

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
        actual val serverTimestamp: FieldValue get() = rethrow { FieldValue(jsServerTimestamp()) }
        actual val delete: FieldValue get() = rethrow { FieldValue(deleteField()) }
        actual fun increment(value: Int): FieldValue = rethrow { FieldValue(jsIncrement(value)) }
        actual fun arrayUnion(vararg elements: Any): FieldValue = rethrow { FieldValue(jsArrayUnion(*elements)) }
        actual fun arrayRemove(vararg elements: Any): FieldValue = rethrow { FieldValue(jsArrayRemove(*elements)) }
    }
}
