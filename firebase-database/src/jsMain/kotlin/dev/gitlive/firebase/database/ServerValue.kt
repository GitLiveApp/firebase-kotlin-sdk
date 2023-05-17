package dev.gitlive.firebase.database

import dev.gitlive.firebase.firebase
import kotlinx.serialization.Serializable

private typealias NativeServerValue = firebase.database.ServerValue

/** Represents a Firebase ServerValue. */
@Serializable(with = ServerValueSerializer::class)
actual class ServerValue internal actual constructor(
    internal actual val nativeValue: Any
){
    actual companion object {
        actual val TIMESTAMP: ServerValue get() = ServerValue(NativeServerValue.TIMESTAMP)
        actual fun increment(delta: Double): ServerValue = ServerValue(NativeServerValue.increment(delta))
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is ServerValue && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = "ServerValue($nativeValue)"
}
