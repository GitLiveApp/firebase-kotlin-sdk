package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseCompositeDecoder
import dev.gitlive.firebase.FirebaseCompositeEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class FirebaseGeoPointSerializer :  KSerializer<GeoPoint> {

    override val descriptor = object : SerialDescriptor {
        val keys = listOf("latitude", "longitude")
        override val kind = StructureKind.OBJECT
        override val serialName = "GeoPoint"
        override val elementsCount get() = 2
        override fun getElementIndex(name: String) = keys.indexOf(name)
        override fun getElementName(index: Int) = keys[index]
        override fun getElementAnnotations(index: Int) = emptyList<Annotation>()
        override fun getElementDescriptor(index: Int) = throw NotImplementedError()
        override fun isElementOptional(index: Int) = false
    }

    override fun serialize(encoder: Encoder, value: GeoPoint) {
        val objectEncoder = encoder.beginStructure(descriptor) as FirebaseCompositeEncoder
        objectEncoder.encodeObject(descriptor, 0, value)
        objectEncoder.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): GeoPoint {
        val objectDecoder = decoder.beginStructure(descriptor) as FirebaseCompositeDecoder
        val latitude = objectDecoder.decodeDoubleElement(descriptor, 0)
        val longitude = objectDecoder.decodeDoubleElement(descriptor, 1)
        objectDecoder.endStructure(descriptor)
        return geoPointWith(latitude, longitude)
    }
}
