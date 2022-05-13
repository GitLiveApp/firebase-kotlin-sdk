package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseDecoder
import dev.gitlive.firebase.FirebaseEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FieldValueSerializer : KSerializer<FieldValue> {
    override val descriptor = buildClassSerialDescriptor("FieldValue") { }

    override fun serialize(encoder: Encoder, value: FieldValue) {
        if (encoder is FirebaseEncoder) {
            encoder.value = value.platformValue
        } else {
            // TODO serialize what we can eg delete and server time stamp?
            throw IllegalArgumentException("This serializer must be used with FirebaseEncoder")
        }
    }

    override fun deserialize(decoder: Decoder): FieldValue {
        return if (decoder is FirebaseDecoder) {
            FieldValue(decoder.value as PlatformFieldValue)
        } else {
            throw IllegalArgumentException("This serializer must be used with FirebaseDecoder")
        }
    }
}