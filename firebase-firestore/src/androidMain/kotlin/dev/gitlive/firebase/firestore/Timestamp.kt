package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase Timestamp. */
actual typealias PlatformTimestamp = com.google.firebase.Timestamp

/** A base class that could be used to combine [Timestamp] and [Timestamp.ServerTimestamp] in the same field. */
@Serializable(with = BaseTimestampSerializer::class)
actual sealed class BaseTimestamp

/** A class representing a Firebase Timestamp. */
@Serializable(with = TimestampSerializer::class)
actual class Timestamp internal actual constructor(
    internal actual val platformValue: PlatformTimestamp
): BaseTimestamp() {
    actual constructor(seconds: Long, nanoseconds: Int) : this(PlatformTimestamp(seconds, nanoseconds))

    actual val seconds: Long = platformValue.seconds
    actual val nanoseconds: Int = platformValue.nanoseconds

    override fun equals(other: Any?): Boolean =
        this === other || other is Timestamp && platformValue == other.platformValue
    override fun hashCode(): Int = platformValue.hashCode()
    override fun toString(): String = platformValue.toString()

    actual companion object {
        actual fun now(): Timestamp = Timestamp(PlatformTimestamp.now())
    }

    /** A server time timestamp. */
    actual object ServerTimestamp: BaseTimestamp()
}
