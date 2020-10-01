package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseCompositeDecoder
import dev.gitlive.firebase.FirebaseCompositeEncoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.StructureKind

class FirebaseNullableTimestampSerializer : KSerializer<Timestamp?> {

    override val descriptor = object : SerialDescriptor {
        val keys = listOf("seconds", "nanoseconds")
        override val kind = StructureKind.OBJECT
        override val serialName = "Timestamp"
        override val elementsCount get() = 2
        override fun getElementIndex(name: String) = keys.indexOf(name)
        override fun getElementName(index: Int) = keys[index]
        override fun getElementAnnotations(index: Int) = emptyList<Annotation>()
        override fun getElementDescriptor(index: Int) = throw NotImplementedError()
        override fun isElementOptional(index: Int) = false
    }

    override fun serialize(encoder: Encoder, value: Timestamp?) {
        val objectEncoder = encoder.beginStructure(descriptor) as FirebaseCompositeEncoder
        objectEncoder.encodeObject(descriptor, 0, value)
        objectEncoder.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Timestamp? {
        val objectDecoder = decoder.beginStructure(descriptor) as FirebaseCompositeDecoder
        return try {
            val seconds = objectDecoder.decodeLongElement(descriptor, 0)
            val nanoseconds = objectDecoder.decodeIntElement(descriptor, 1)
            objectDecoder.endStructure(descriptor)
            timestampWith(seconds, nanoseconds)
        } catch (exception: SerializationException) {
            objectDecoder.endStructure(descriptor)
            null
        }
    }
}
