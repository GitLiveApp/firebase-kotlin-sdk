package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseEncoder
import dev.gitlive.firebase.SpecialValueSerializer
import dev.gitlive.firebase.firestore.DoubleAsTimestampSerializer.serverTimestamp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException

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
