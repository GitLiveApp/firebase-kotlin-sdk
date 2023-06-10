package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseDecoder
import dev.gitlive.firebase.FirebaseEncoder
import dev.gitlive.firebase.SpecialValueSerializer
import dev.gitlive.firebase.firestore.DoubleAsTimestampSerializer.serverTimestamp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
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

/** A serializer for [BaseTimestamp]. Must be used with [FirebaseEncoder]/[FirebaseDecoder]. */
object BaseTimestampSerializer : KSerializer<BaseTimestamp> by SpecialValueSerializer(
    serialName = "Timestamp",
    toNativeValue = { value ->
        when (value) {
            Timestamp.ServerTimestamp -> FieldValue.serverTimestamp.nativeValue
            is Timestamp -> value.nativeValue
            else -> throw SerializationException("Cannot serialize $value")
        }
    },
    fromNativeValue = { value ->
        when (value) {
            is NativeTimestamp -> Timestamp(value)
            FieldValue.serverTimestamp.nativeValue -> Timestamp.ServerTimestamp
            else -> throw SerializationException("Cannot deserialize $value")
        }
    }
)

/** A serializer for [Timestamp]. Must be used with [FirebaseEncoder]/[FirebaseDecoder]. */
object TimestampSerializer : KSerializer<Timestamp> by SpecialValueSerializer(
    serialName = "Timestamp",
    toNativeValue = Timestamp::nativeValue,
    fromNativeValue = { value ->
        when (value) {
            is NativeTimestamp -> Timestamp(value)
            else -> throw SerializationException("Cannot deserialize $value")
        }
    }
)

/** A serializer for [Timestamp.ServerTimestamp]. Must be used with [FirebaseEncoder]/[FirebaseDecoder]. */
object ServerTimestampSerializer : KSerializer<Timestamp.ServerTimestamp> by SpecialValueSerializer(
    serialName = "Timestamp",
    toNativeValue = { FieldValue.serverTimestamp.nativeValue },
    fromNativeValue = { value ->
        when (value) {
            FieldValue.serverTimestamp.nativeValue -> Timestamp.ServerTimestamp
            else -> throw SerializationException("Cannot deserialize $value")
        }
    }
)

/** A serializer for a Double field which is stored as a Timestamp. */
object DoubleAsTimestampSerializer : KSerializer<Double> by SpecialValueSerializer(
    serialName = "Timestamp",
    toNativeValue = { value ->
        when(value) {
            serverTimestamp -> FieldValue.serverTimestamp.nativeValue
            else -> Timestamp.fromMilliseconds(value).nativeValue
        }
    },
    fromNativeValue = { value ->
        when(value) {
            FieldValue.serverTimestamp.nativeValue -> serverTimestamp
            is NativeTimestamp -> Timestamp(value).toMilliseconds()
            is Double -> value
            else -> throw SerializationException("Cannot deserialize $value")
        }
    }
) {
    const val serverTimestamp = Double.POSITIVE_INFINITY
}
