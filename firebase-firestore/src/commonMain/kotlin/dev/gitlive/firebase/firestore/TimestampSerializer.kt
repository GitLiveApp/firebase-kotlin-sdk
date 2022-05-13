package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseDecoder
import dev.gitlive.firebase.FirebaseEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/** A serializer for [Timestamp]. If used with [FirebaseEncoder] performs serialization using native Firebase mechanisms. */
object TimestampSerializer : KSerializer<Timestamp> {
    override val descriptor = buildClassSerialDescriptor("Timestamp") {
        element<Long>("seconds")
        element<Int>("nanoseconds")
        element<Boolean>("isServerTimestamp")
    }

    override fun serialize(encoder: Encoder, value: Timestamp) {
        if (encoder is FirebaseEncoder) {
            // special case if encoding. Firestore encodes and decodes Timestamp without use of serializers
            encoder.value = if (value.isServerTimestamp) {
                FieldValue.serverTimestamp().platformValue
            } else {
                value.platformValue
            }
        } else {
            encoder.encodeStructure(descriptor) {
                encodeLongElement(descriptor, 0, value.seconds)
                encodeIntElement(descriptor, 1, value.nanoseconds)
                encodeBooleanElement(descriptor, 2, value.isServerTimestamp)
            }
        }
    }

    override fun deserialize(decoder: Decoder): Timestamp {
        return if (decoder is FirebaseDecoder) {
            // special case if decoding. Firestore encodes and decodes Timestamp without use of serializers
            when (val value = decoder.value) {
                is PlatformTimestamp -> Timestamp(value)
                FieldValue.serverTimestamp().platformValue -> Timestamp.serverTimestamp()
                else -> throw SerializationException("Cannot serialize $value")
            }
        } else {
            decoder.decodeStructure(descriptor) {
                if (decodeBooleanElement(descriptor, 2)) {
                    Timestamp.serverTimestamp()
                } else {
                    Timestamp(
                        seconds = decodeLongElement(descriptor, 0),
                        nanoseconds = decodeIntElement(descriptor, 1)
                    )
                }
            }
        }
    }
}
