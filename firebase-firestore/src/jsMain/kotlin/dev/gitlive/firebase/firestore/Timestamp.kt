package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.serialization.Serializable

/** A base class that could be used to combine [Timestamp] and [Timestamp.ServerTimestamp] in the same field. */
@Serializable(with = BaseTimestampSerializer::class)
actual sealed class BaseTimestamp

/** A class representing a platform specific Firebase Timestamp. */
actual typealias PlatformTimestamp = firebase.firestore.Timestamp

/** A class representing a Firebase Timestamp. */
@Serializable(with = TimestampSerializer::class)
actual class Timestamp internal actual constructor(
    internal actual val platformValue: PlatformTimestamp
): BaseTimestamp() {
    actual constructor(seconds: Long, nanoseconds: Int) : this(PlatformTimestamp(seconds.toDouble(), nanoseconds.toDouble()))

    actual val seconds: Long = platformValue.seconds.toLong()
    actual val nanoseconds: Int = platformValue.nanoseconds.toInt()

    override fun equals(other: Any?): Boolean =
        this === other || other is Timestamp && platformValue.isEqual(other.platformValue)
    override fun hashCode(): Int = platformValue.hashCode()
    override fun toString(): String = platformValue.toString()

    actual companion object {
        actual fun now(): Timestamp = Timestamp(PlatformTimestamp.now())
    }

    /** A server time timestamp. */
    actual object ServerTimestamp: BaseTimestamp()
}

