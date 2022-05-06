package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseDecoder
import dev.gitlive.firebase.FirebaseEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

abstract class AbstractFirebaseTimestampSerializer<T>(
    private val isNullable: Boolean
) : KSerializer<T> {
    override val descriptor = buildClassSerialDescriptor("Timestamp", isNullable = isNullable) {
        isNullable = this@AbstractFirebaseTimestampSerializer.isNullable
        element<Long>("seconds")
        element<Int>("nanoseconds")
    }

    protected fun encode(encoder: Encoder, value: Any?) {
        when(value) {
            is Timestamp -> encodeTimestamp(encoder, value)
            null -> encodeTimestamp(encoder, null)
            else -> {
                if (isSpecialValue(value) && encoder is FirebaseEncoder) {
                    encoder.value = value
                } else {
                    throw SerializationException("Cannot serialize $value")
                }
            }
        }
    }

    private fun encodeTimestamp(encoder: Encoder, value: Timestamp?) {
        require(value != null || isNullable)

        if (encoder is FirebaseEncoder) {
            // special case if encoding. Firestore encodes and decodes GeoPoints without use of serializers
            encoder.value = value
        } else {
            if (value != null) {
                encoder.encodeStructure(descriptor) {
                    encodeLongElement(descriptor, 0, value.seconds)
                    encodeIntElement(descriptor, 1, value.nanoseconds)
                }
            } else {
                encoder.encodeNull()
            }
        }
    }

    protected fun decode(decoder: Decoder): Timestamp? {
        return if (decoder is FirebaseDecoder) {
            // special case if decoding. Firestore encodes and decodes GeoPoints without use of serializers
            when (val value = decoder.value) {
                null -> null
                is Timestamp -> value
                else -> {
                    if (isSpecialValue(value)) {
                        null
                    } else {
                        throw SerializationException("Cannot deserialize $value")
                    }
                }
            }
        } else {
            decoder.decodeStructure(descriptor) {
                timestampWith(
                    seconds = decodeLongElement(descriptor, 0),
                    nanoseconds = decodeIntElement(descriptor, 1)
                )
            }
        }
    }
}

object FirebaseTimestampSerializer : AbstractFirebaseTimestampSerializer<Any>(
    isNullable = false
) {
    override fun serialize(encoder: Encoder, value: Any) = encode(encoder, value)
    override fun deserialize(decoder: Decoder): Any = requireNotNull(decode(decoder))
}

object FirebaseNullableTimestampSerializer : AbstractFirebaseTimestampSerializer<Any?>(
    isNullable = true
) {
    override fun serialize(encoder: Encoder, value: Any?) = encode(encoder, value)

    override fun deserialize(decoder: Decoder): Any? {
        return try {
            decode(decoder)
        } catch (exception: SerializationException) {
            null
        }
    }
}
