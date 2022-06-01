package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseEncoder
import dev.gitlive.firebase.SpecialValueSerializer
import dev.gitlive.firebase.firestore.DoubleAsTimestampSerializer.serverTimestamp
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

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

fun Timestamp.Companion.fromMilliseconds(milliseconds: Double): Timestamp =
    Timestamp((milliseconds / 1000).toLong(), (milliseconds * 1000).toInt() % 1000000)
fun Timestamp.toMilliseconds(): Double = seconds * 1000 + (nanoseconds / 1000.0)

/** A serializer for [BaseTimestamp]. If used with [FirebaseEncoder] performs serialization using native Firebase mechanisms. */
object BaseTimestampSerializer : SpecialValueSerializer<BaseTimestamp>(
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

/** A serializer for [Timestamp]. If used with [FirebaseEncoder] performs serialization using native Firebase mechanisms. */
object TimestampSerializer : SpecialValueSerializer<Timestamp>(
    serialName = "Timestamp",
    toNativeValue = Timestamp::nativeValue,
    fromNativeValue = { value ->
        when (value) {
            is NativeTimestamp -> Timestamp(value)
            else -> throw SerializationException("Cannot deserialize $value")
        }
    }
)

/** A serializer for [Timestamp.ServerTimestamp]. If used with [FirebaseEncoder] performs serialization using native Firebase mechanisms. */
object ServerTimestampSerializer : SpecialValueSerializer<Timestamp.ServerTimestamp>(
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
object DoubleAsTimestampSerializer : SpecialValueSerializer<Double>(
    serialName = "Timestamp",
    toNativeValue = { value ->
        when(value) {
            serverTimestamp -> FieldValue.serverTimestamp.nativeValue
            else -> Timestamp.fromMilliseconds(value)
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
