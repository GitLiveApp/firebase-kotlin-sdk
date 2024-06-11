package dev.gitlive.firebase.database

import dev.gitlive.firebase.internal.FirebaseDecoder
import dev.gitlive.firebase.internal.FirebaseEncoder
import dev.gitlive.firebase.internal.SpecialValueSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/** Represents a Firebase ServerValue. */
@Serializable(with = ServerValueSerializer::class)
expect class ServerValue internal constructor(nativeValue: Any) {
    internal val nativeValue: Any

    companion object {
        val TIMESTAMP: ServerValue
        fun increment(delta: Double): ServerValue
    }
}

/** Serializer for [ServerValue]. Must be used with [FirebaseEncoder]/[FirebaseDecoder].*/
object ServerValueSerializer : KSerializer<ServerValue> by SpecialValueSerializer(
    serialName = "ServerValue",
    toNativeValue = ServerValue::nativeValue,
    fromNativeValue = { raw ->
        raw?.let(::ServerValue) ?: throw SerializationException("Cannot deserialize $raw")
    },
)
