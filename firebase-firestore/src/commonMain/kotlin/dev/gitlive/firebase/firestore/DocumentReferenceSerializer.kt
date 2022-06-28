package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/**
 * A serializer for [DocumentReference]. If used with [FirebaseEncoder] performs serialization using native Firebase mechanisms.
 */
object DocumentReferenceSerializer : KSerializer<DocumentReference> {

    override val descriptor = buildClassSerialDescriptor("DocumentReference") {
        element<String>("path")
    }

    override fun serialize(encoder: Encoder, value: DocumentReference) {
        if (encoder is FirebaseEncoder) {
            // special case if decoding. Firestore encodes and decodes DocumentReferences without use of serializers
            encoder.value = value.nativeValue
        } else {
            encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, value.path)
            }
        }
    }

    override fun deserialize(decoder: Decoder): DocumentReference {
        return if (decoder is FirebaseDecoder) {
            // special case if decoding. Firestore encodes and decodes DocumentReferences without use of serializers
            when (val value = decoder.value) {
                is NativeDocumentReference -> DocumentReference(value)
                else -> throw SerializationException("Cannot deserialize $value")
            }
        } else {
            decoder.decodeStructure(descriptor) {
                val path = decodeStringElement(descriptor, 0)
                Firebase.firestore.document(path)
            }
        }
    }
}
