package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseEncoder
import dev.gitlive.firebase.SpecialValueSerializer
import dev.gitlive.firebase.firestore.DoubleAsTimestampSerializer.serverTimestamp
import kotlinx.serialization.SerializationException

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
