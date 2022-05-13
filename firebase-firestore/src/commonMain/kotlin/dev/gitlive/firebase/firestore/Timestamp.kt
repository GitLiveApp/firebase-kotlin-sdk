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
    val isServerTimestamp: Boolean

    internal val platformValue: PlatformTimestamp

    companion object {
        fun now(): Timestamp
        fun serverTimestamp(): Timestamp
    }
}
