package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseDecoder
import dev.gitlive.firebase.FirebaseEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/** Serializer for [GeoPoint].If used with [FirebaseEncoder] performs serialization using native Firebase mechanisms. */
object GeoPointSerializer : KSerializer<GeoPoint> {
    override val descriptor = buildClassSerialDescriptor("GeoPoint") {
        element<Double>("latitude")
        element<Double>("longitude")
    }

    override fun serialize(encoder: Encoder, value: GeoPoint) {
        if (encoder is FirebaseEncoder) {
            // special case if encoding. Firestore encodes and decodes GeoPoints without use of serializers
            encoder.value = value.platformValue
        } else {
            encoder.encodeStructure(descriptor) {
                encodeDoubleElement(descriptor, 0, value.latitude)
                encodeDoubleElement(descriptor, 1, value.longitude)
            }
        }
    }

    override fun deserialize(decoder: Decoder): GeoPoint {
        return if (decoder is FirebaseDecoder) {
            // special case if decoding. Firestore encodes and decodes GeoPoints without use of serializers
            GeoPoint(decoder.value as PlatformGeoPoint)
        } else {
            decoder.decodeStructure(descriptor) {
                GeoPoint(
                    latitude = decodeDoubleElement(descriptor, 0),
                    longitude = decodeDoubleElement(descriptor, 1)
                )
            }
        }
    }
}
