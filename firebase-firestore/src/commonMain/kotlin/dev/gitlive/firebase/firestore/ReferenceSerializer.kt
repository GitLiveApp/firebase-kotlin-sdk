package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseCompositeDecoder
import dev.gitlive.firebase.FirebaseCompositeEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class AbstractFirebaseReferenceSerializer<T> : KSerializer<T> {

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

    abstract override fun serialize(encoder: Encoder, value: T)
    abstract override fun deserialize(decoder: Decoder): T

    fun encode(encoder: Encoder, value: FirebaseReference?) {
        val objectEncoder = encoder.beginStructure(descriptor) as FirebaseCompositeEncoder
        val documentReferenceEncoder = FirebaseDocumentReferenceEncoder()
        when (value) {
            is FirebaseReference.Value ->
                objectEncoder.encodeObject(descriptor, 0, documentReferenceEncoder.encode(value.value))
            is FirebaseReference.ServerDelete ->
                objectEncoder.encodeObject(descriptor, 0, FieldValue.delete)
        }
        objectEncoder.endStructure(descriptor)
    }
}

class FirebaseReferenceNullableSerializer : AbstractFirebaseReferenceSerializer<FirebaseReference?>() {

    override fun serialize(encoder: Encoder, value: FirebaseReference?) {
        super.encode(encoder, value)
    }

    override fun deserialize(decoder: Decoder): FirebaseReference? {
        val objectDecoder = decoder.beginStructure(descriptor) as FirebaseCompositeDecoder
        return try {
            val path = objectDecoder.decodeStringElement(descriptor, 0)
            FirebaseReference.Value(Firebase.firestore.document(path))
        } catch (exception: Exception) {
            null
        } finally {
            objectDecoder.endStructure(descriptor)
        }
    }
}

class FirebaseReferenceSerializer : AbstractFirebaseReferenceSerializer<FirebaseReference>() {

    override fun serialize(encoder: Encoder, value: FirebaseReference) {
        super.encode(encoder, value)
    }

    override fun deserialize(decoder: Decoder): FirebaseReference {
        val objectDecoder = decoder.beginStructure(descriptor) as FirebaseCompositeDecoder
        val path = objectDecoder.decodeStringElement(descriptor, 0)
        objectDecoder.endStructure(descriptor)
        return FirebaseReference.Value(Firebase.firestore.document(path))
    }
}
