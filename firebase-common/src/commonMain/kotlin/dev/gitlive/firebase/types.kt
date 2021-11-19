package dev.gitlive.firebase

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = FirebaseTimestampSerializer::class)
class Timestamp(
    val nanoseconds: Int,
    val seconds: Long
) {
    fun test(){}
}

expect fun Timestamp.asNative() : Any

private object TimestampDescriptor : SerialDescriptor {
    override val kind = StructureKind.OBJECT
    override val serialName = "firebaseTimestamp"
    override val elementsCount get() = 2
    override fun getElementIndex(name: String) = when (name) {
        "nanos" -> 0
        "seconds" -> 1
        else -> -1
    }
    override fun getElementName(index: Int) = when (index) {
        0 -> "nanos"
        1 -> "seconds"
        else -> throw IndexOutOfBoundsException()
    }
    override fun getElementAnnotations(index: Int) = emptyList<Annotation>()
    override fun getElementDescriptor(index: Int) = throw NotImplementedError()
    override fun isElementOptional(index: Int) = false
}

private object FirebaseTimestampSerializer : KSerializer<Timestamp> {
    override val descriptor = TimestampDescriptor

    override fun serialize(encoder: Encoder, value: Timestamp) {
        println("Serializing timestamp")
        val collection = encoder.beginCollection(descriptor, 2)
        collection.encodeIntElement(descriptor, 0, value.nanoseconds)
        collection.encodeLongElement(descriptor, 1, value.seconds)
        collection.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Timestamp {
        throw NotImplementedError()
    }
}