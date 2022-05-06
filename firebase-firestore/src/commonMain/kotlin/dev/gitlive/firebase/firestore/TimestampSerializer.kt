package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseDecoder
import dev.gitlive.firebase.FirebaseEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class AbstractTimestampSerializer<T>(
    private val isNullable: Boolean
) : KSerializer<T> {

    override val descriptor = buildClassSerialDescriptor("Timestamp", isNullable = isNullable) {
        isNullable = this@AbstractTimestampSerializer.isNullable
        element<Long>("seconds")
        element<Int>("nanoseconds")
    }

    protected fun encode(encoder: Encoder, value: FirebaseTimestamp?) {
        if (encoder is FirebaseEncoder) {
            encoder.value = value?.let {
                when(value) {
                    is FirebaseTimestamp.Value -> value.value
                    is FirebaseTimestamp.ServerValue -> FieldValue.serverTimestamp()
                    is FirebaseTimestamp.ServerDelete -> FieldValue.delete
                }
            }
        } else {
            throw IllegalArgumentException("This serializer shall be used with FirebaseEncoder")
        }
    }

    protected fun decode(decoder: Decoder): FirebaseTimestamp? {
        return if (decoder is FirebaseDecoder) {
            (decoder.value as? Timestamp)?.let(FirebaseTimestamp::Value)
        } else {
            throw IllegalArgumentException("This serializer shall be used with FirebaseEncoder")
        }
    }
}

object TimestampNullableSerializer : AbstractTimestampSerializer<FirebaseTimestamp?>(
    isNullable = true
) {

    override fun serialize(encoder: Encoder, value: FirebaseTimestamp?) = encode(encoder, value)

    override fun deserialize(decoder: Decoder): FirebaseTimestamp? {
        return try {
            decode(decoder)
        } catch (exception: SerializationException) {
            null
        }
    }
}

object TimestampSerializer : AbstractTimestampSerializer<FirebaseTimestamp>(
    isNullable = false
) {
    override fun serialize(encoder: Encoder, value: FirebaseTimestamp) = encode(encoder, value)
    override fun deserialize(decoder: Decoder): FirebaseTimestamp = requireNotNull(decode(decoder))
}
