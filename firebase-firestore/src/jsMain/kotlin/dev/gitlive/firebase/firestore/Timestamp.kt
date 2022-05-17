package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase Timestamp. */
actual typealias PlatformTimestamp = firebase.firestore.Timestamp

/** A class representing a Firebase Timestamp. */
@Serializable(with = TimestampSerializer::class)
actual class Timestamp private constructor(
    internal actual val platformValue: PlatformTimestamp,
    actual val isServerTimestamp: Boolean
) {
    internal actual constructor(platformValue: PlatformTimestamp) : this(platformValue, false)
    actual constructor(seconds: Long, nanoseconds: Int) : this(PlatformTimestamp(seconds.toDouble(), nanoseconds.toDouble()))

    actual val seconds: Long = platformValue.seconds.toLong()
    actual val nanoseconds: Int = platformValue.nanoseconds.toInt()

    override fun equals(other: Any?): Boolean =
        this === other || other is Timestamp && isServerTimestamp == other.isServerTimestamp &&
                platformValue.isEqual(other.platformValue)
    override fun hashCode(): Int = platformValue.hashCode()
    override fun toString(): String = if (isServerTimestamp) {
        "Timestamp(serverTimestamp)"
    } else {
        platformValue.toString()
    }

    actual companion object {
        actual fun now(): Timestamp = Timestamp(PlatformTimestamp.now())
        actual fun serverTimestamp(): Timestamp = Timestamp(PlatformTimestamp(0.0, 0.0), true)

        private fun equals(one: Timestamp, another: Timestamp): Boolean =
            one.isServerTimestamp == another.isServerTimestamp && one.seconds == another.seconds && one.nanoseconds == another.nanoseconds
    }
}

