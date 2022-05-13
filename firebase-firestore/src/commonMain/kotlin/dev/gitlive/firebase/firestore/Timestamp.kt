package dev.gitlive.firebase.firestore

/** A class representing a platform specific Firebase Timestamp. */
expect class PlatformTimestamp

/** A class representing a Firebase Timestamp. */
expect class Timestamp internal constructor(platformValue: PlatformTimestamp) {
    constructor(seconds: Long, nanoseconds: Int)
    val seconds: Long
    val nanoseconds: Int

    internal val platformValue: PlatformTimestamp

    companion object {
        fun now(): Timestamp
    }
}
