package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.serialization.Serializable

/** A base class that could be used to combine [Timestamp] and [Timestamp.ServerTimestamp] in the same field. */
@Serializable(with = BaseTimestampSerializer::class)
actual sealed class BaseTimestamp

/** A class representing a platform specific Firebase Timestamp. */
actual typealias NativeTimestamp = firebase.firestore.Timestamp

/** A class representing a Firebase Timestamp. */
@Serializable(with = TimestampSerializer::class)
actual class Timestamp internal actual constructor(
    internal actual val nativeValue: NativeTimestamp
): BaseTimestamp() {
    actual constructor(seconds: Long, nanoseconds: Int) : this(NativeTimestamp(seconds.toDouble(), nanoseconds.toDouble()))

    actual val seconds: Long = nativeValue.seconds.toLong()
    actual val nanoseconds: Int = nativeValue.nanoseconds.toInt()

    override fun equals(other: Any?): Boolean =
        this === other || other is Timestamp && nativeValue.isEqual(other.nativeValue)
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    actual companion object {
        actual fun now(): Timestamp = Timestamp(NativeTimestamp.now())
    }

    /** A server time timestamp. */
    @Serializable(with = ServerTimestampSerializer::class)
    actual object ServerTimestamp: BaseTimestamp()
}

