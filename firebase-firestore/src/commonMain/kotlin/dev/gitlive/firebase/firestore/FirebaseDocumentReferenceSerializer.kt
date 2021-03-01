package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseCompositeDecoder
import dev.gitlive.firebase.FirebaseCompositeEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

expect class FirebaseDocumentReferenceEncoder() {
    fun encode(value: DocumentReference): Any
}

class FirebaseDocumentReferenceSerializer : KSerializer<DocumentReference> {

    override val descriptor = object : SerialDescriptor {
        val keys = listOf("path")
        override val kind = StructureKind.OBJECT
        override val serialName = "DocumentReference"
        override val elementsCount get() = 1
        override fun getElementIndex(name: String) = keys.indexOf(name)
        override fun getElementName(index: Int) = keys[index]
        override fun getElementAnnotations(index: Int) = emptyList<Annotation>()
        override fun getElementDescriptor(index: Int) = throw NotImplementedError()
        override fun isElementOptional(index: Int) = false
    }

    override fun serialize(encoder: Encoder, value: DocumentReference) {
        val objectEncoder = encoder.beginStructure(descriptor) as FirebaseCompositeEncoder
        val documentReferenceEncoder = FirebaseDocumentReferenceEncoder()
        objectEncoder.encodeObject(descriptor, 0, documentReferenceEncoder.encode(value))
        objectEncoder.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): DocumentReference {
        val objectDecoder = decoder.beginStructure(descriptor) as FirebaseCompositeDecoder
        val path = objectDecoder.decodeStringElement(descriptor, 0)
        objectDecoder.endStructure(descriptor)
        return Firebase.firestore.document(path)
    }
}
