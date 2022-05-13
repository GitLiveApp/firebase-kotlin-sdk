package dev.gitlive.firebase.firestore

/** A class representing a platform specific Firebase Timestamp. */
actual typealias PlatformTimestamp = com.google.firebase.Timestamp

/** A class representing a Firebase Timestamp. */
actual class Timestamp internal actual constructor(internal actual val platformValue: PlatformTimestamp) {
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
}
