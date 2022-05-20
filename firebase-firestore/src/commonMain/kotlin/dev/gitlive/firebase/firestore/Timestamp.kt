package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseEncoder
import dev.gitlive.firebase.SpecialValueSerializer
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
