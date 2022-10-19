package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase Timestamp. */
expect class NativeTimestamp

/** A base class that could be used to combine [Timestamp] and [Timestamp.ServerTimestamp] in the same field. */
@Serializable(with = BaseTimestampSerializer::class)
expect sealed class BaseTimestamp

/** A class representing a Firebase Timestamp. */
@Serializable(with = TimestampSerializer::class)
expect class Timestamp internal constructor(nativeValue: NativeTimestamp): BaseTimestamp {
    constructor(seconds: Long, nanoseconds: Int)
    val seconds: Long
    val nanoseconds: Int

    internal val nativeValue: NativeTimestamp

    companion object {
        /** @return a local time timestamp. */
        fun now(): Timestamp
    }
    /** A server time timestamp. */
    @Serializable(with = ServerTimestampSerializer::class)
    object ServerTimestamp: BaseTimestamp
}

private const val FACTOR = 1000000
fun Timestamp.Companion.fromMilliseconds(milliseconds: Long): Timestamp =
    Timestamp(milliseconds / 1000, ((milliseconds % 1000) * FACTOR).toInt())
fun Timestamp.toMilliseconds(): Long = seconds * 1000 + nanoseconds / FACTOR
