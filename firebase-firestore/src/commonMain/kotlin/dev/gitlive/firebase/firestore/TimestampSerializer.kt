package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseCompositeDecoder
import dev.gitlive.firebase.FirebaseCompositeEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class AbstractTimestampSerializer<T> : KSerializer<T> {

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

    abstract override fun serialize(encoder: Encoder, value: T)
    abstract override fun deserialize(decoder: Decoder): T

    fun encode(encoder: Encoder, value: FirebaseTimestamp?) {
        val objectEncoder = encoder.beginStructure(descriptor) as FirebaseCompositeEncoder
        when (value) {
            is FirebaseTimestamp.Value -> {
                objectEncoder.encodeObject(descriptor, 0, value.value)
            }
            is FirebaseTimestamp.ServerValue -> {
                objectEncoder.encodeObject(descriptor, 0, FieldValue.serverTimestamp())
            }
        }
        objectEncoder.endStructure(descriptor)
    }
}

class TimestampNullableSerializer : AbstractTimestampSerializer<FirebaseTimestamp?>() {

    override fun serialize(encoder: Encoder, value: FirebaseTimestamp?) {
        super.encode(encoder, value)
    }

    override fun deserialize(decoder: Decoder): FirebaseTimestamp? {
        val objectDecoder = decoder.beginStructure(descriptor) as FirebaseCompositeDecoder
        return try {
            val seconds = objectDecoder.decodeLongElement(descriptor, 0)
            val nanoseconds = objectDecoder.decodeIntElement(descriptor, 1)
            FirebaseTimestamp.Value(timestampWith(seconds, nanoseconds))
        } catch (exception: SerializationException) {
            null
        } finally {
            objectDecoder.endStructure(descriptor)
        }
    }
}

class TimestampSerializer : AbstractTimestampSerializer<FirebaseTimestamp>() {

    override fun serialize(encoder: Encoder, value: FirebaseTimestamp) {
        super.encode(encoder, value)
    }

    override fun deserialize(decoder: Decoder): FirebaseTimestamp {
        val objectDecoder = decoder.beginStructure(descriptor) as FirebaseCompositeDecoder
        val seconds = objectDecoder.decodeLongElement(descriptor, 0)
        val nanoseconds = objectDecoder.decodeIntElement(descriptor, 1)
        objectDecoder.endStructure(descriptor)
        return FirebaseTimestamp.Value(timestampWith(seconds, nanoseconds))
    }
}
