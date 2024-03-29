package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

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

fun Timestamp.Companion.fromDuration(duration: Duration): Timestamp =
    duration.toComponents { seconds, nanoseconds ->
        Timestamp(seconds, nanoseconds)
    }
fun Timestamp.toDuration(): Duration = seconds.seconds + nanoseconds.nanoseconds

fun Timestamp.Companion.fromMilliseconds(milliseconds: Double): Timestamp = fromDuration(milliseconds.milliseconds)
fun Timestamp.toMilliseconds(): Double = toDuration().toDouble(DurationUnit.MILLISECONDS)
