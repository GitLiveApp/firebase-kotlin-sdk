package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase Timestamp. */
expect class PlatformTimestamp

/** A base class that could be used to combine [Timestamp] and [Timestamp.ServerTimestamp] in the same field. */
@Serializable(with = BaseTimestampSerializer::class)
expect sealed class BaseTimestamp

/** A class representing a Firebase Timestamp. */
@Serializable(with = TimestampSerializer::class)
expect class Timestamp internal constructor(platformValue: PlatformTimestamp): BaseTimestamp {
    constructor(seconds: Long, nanoseconds: Int)
    val seconds: Long
    val nanoseconds: Int

    internal val platformValue: PlatformTimestamp

    companion object {
        /** @return a local time timestamp. */
        fun now(): Timestamp
    }
    /** A server time timestamp. */
    object ServerTimestamp: BaseTimestamp
}
