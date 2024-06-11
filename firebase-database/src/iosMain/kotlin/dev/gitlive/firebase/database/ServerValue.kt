package dev.gitlive.firebase.database

import cocoapods.FirebaseDatabase.FIRServerValue
import kotlinx.serialization.Serializable
import platform.Foundation.NSNumber

private typealias NativeServerValue = FIRServerValue

/** Represents a Firebase ServerValue. */
@Serializable(with = ServerValueSerializer::class)
actual class ServerValue internal actual constructor(
    internal actual val nativeValue: Any,
) {
    actual companion object {
        actual val TIMESTAMP: ServerValue get() = ServerValue(NativeServerValue.timestamp())

        @Suppress("CAST_NEVER_SUCCEEDS")
        actual fun increment(delta: Double): ServerValue = ServerValue(NativeServerValue.increment(delta as NSNumber))
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is ServerValue && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = "ServerValue($nativeValue)"
}
