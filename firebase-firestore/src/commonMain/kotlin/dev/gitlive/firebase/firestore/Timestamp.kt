package dev.gitlive.firebase.firestore

import com.google.firebase.firestore.timestampNow
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/** A class representing a platform specific Firebase Timestamp; the Android-SDK-shaped [com.google.firebase.Timestamp]. */
public typealias NativeTimestamp = com.google.firebase.Timestamp

/** A base class that could be used to combine [Timestamp] and [Timestamp.ServerTimestamp] in the same field. */
@Serializable(with = BaseTimestampSerializer::class)
public sealed class BaseTimestamp

/** A class representing a Firebase Timestamp. */
@Serializable(with = TimestampSerializer::class)
public class Timestamp internal constructor(internal val nativeValue: NativeTimestamp) : BaseTimestamp() {
    public constructor(seconds: Long, nanoseconds: Int) : this(NativeTimestamp(seconds, nanoseconds))

    /** The Android-SDK-shaped [com.google.firebase.Timestamp] this wraps. */
    public val compat: NativeTimestamp get() = nativeValue

    public val seconds: Long get() = nativeValue.seconds
    public val nanoseconds: Int get() = nativeValue.nanoseconds

    override fun equals(other: Any?): Boolean = this === other || other is Timestamp && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    public companion object {
        /** @return a local time timestamp. */
        public fun now(): Timestamp = Timestamp(timestampNow())
    }

    /** A server time timestamp. */
    @Serializable(with = ServerTimestampSerializer::class)
    public data object ServerTimestamp : BaseTimestamp()
}

public fun Timestamp.Companion.fromDuration(duration: Duration): Timestamp = duration.toComponents { seconds, nanoseconds ->
    Timestamp(seconds, nanoseconds)
}
public fun Timestamp.toDuration(): Duration = seconds.seconds + nanoseconds.nanoseconds

public fun Timestamp.Companion.fromMilliseconds(milliseconds: Double): Timestamp = fromDuration(milliseconds.milliseconds)
public fun Timestamp.toMilliseconds(): Double = toDuration().toDouble(DurationUnit.MILLISECONDS)
