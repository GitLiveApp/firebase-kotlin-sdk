package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase Timestamp. */
expect class PlatformTimestamp

/** A class representing a Firebase Timestamp. */
@Serializable(with = TimestampSerializer::class)
expect class Timestamp internal constructor(platformValue: PlatformTimestamp) {
    constructor(seconds: Long, nanoseconds: Int)
    val seconds: Long
    val nanoseconds: Int
    /**
     * Whether timestamp represents a server time stamp.
     * if `true` seconds and nanoseconds do not represent any meaningful data.
     */
    val isServerTimestamp: Boolean

    internal val platformValue: PlatformTimestamp

    companion object {
        /** @return a local time timestamp. */
        fun now(): Timestamp
        /**
         * @return a server time timestamp.
         * `seconds` and `nanoseconds` do not represent a valid data until the field is stored and read.
         */
        fun serverTimestamp(): Timestamp
    }
}
