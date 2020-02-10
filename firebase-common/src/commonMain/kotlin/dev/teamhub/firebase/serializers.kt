package dev.teamhub.firebase

import kotlinx.serialization.*
import kotlinx.serialization.internal.UnitSerializer
import kotlinx.serialization.internal.defaultSerializer
import kotlinx.serialization.internal.nullable

@Suppress("UNCHECKED_CAST")
fun Any.firebaseSerializer() = (this::class.compiledSerializer() ?: this::class.defaultSerializer() ?: when(this) {
    is Map<*, *> -> FirebaseMapSerializer()
    is List<*> -> FirebaseListSerializer()
    is Set<*> -> FirebaseListSerializer()
    else -> throw SerializationException("Can't locate argument-less serializer for $this. For generic classes, such as lists, please provide serializer explicitly.")
}) as KSerializer<Any>

class FirebaseMapSerializer : KSerializer<Map<String, Any?>> {

    lateinit var keys: List<String>
    lateinit var map: Map<String, Any?>

    override val descriptor = object : SerialDescriptor {
        override val kind = StructureKind.MAP
        override val name = "kotlin.Map<String, Any>"
        override fun getElementIndex(name: String) = keys.indexOf(name)
        override fun getElementName(index: Int) = keys[index]
        override val elementsCount get() = map.size
    }

    override fun serialize(encoder: Encoder, obj: Map<String, Any?>) {
        map = obj
        keys = obj.keys.toList()
        val collectionEncoder = encoder.beginCollection(descriptor, obj.size)
        keys.forEachIndexed { index, key ->
            val value = map.getValue(key)
            val serializer = value?.firebaseSerializer() ?: UnitSerializer.nullable as KSerializer<Any>
            collectionEncoder.encodeNullableSerializableElement(
                serializer.descriptor, index, serializer, value
            )
        }
    }

    override fun deserialize(decoder: Decoder): Map<String, Any?> {
        val collectionDecoder = decoder.beginStructure(descriptor) as FirebaseCompositeDecoder
        val map = mutableMapOf<String, Any?>()
        for(index in 0 until collectionDecoder.decodeCollectionSize(descriptor) * 2 step 2) {
            map[collectionDecoder.decodeNullableSerializableElement(index) as String] =
                collectionDecoder.decodeNullableSerializableElement(index + 1)
        }
        return map
    }
}

class FirebaseListSerializer : KSerializer<Iterable<Any?>> {

    lateinit var list: List<Any?>

    override val descriptor= object : SerialDescriptor {
        override val kind = StructureKind.LIST
        override val name = "kotlin.List<Any>"
        override fun getElementIndex(name: String) = throw NotImplementedError()
        override fun getElementName(index: Int) = throw NotImplementedError()
        override val elementsCount get() = list.size
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(encoder: Encoder, obj: Iterable<Any?>) {
        list = obj.toList()
        val collectionEncoder = encoder.beginCollection(descriptor, list.size)
        list.forEachIndexed { index, value ->
            val serializer = value?.firebaseSerializer() ?: UnitSerializer.nullable as KSerializer<Any>
            collectionEncoder.encodeNullableSerializableElement(
                serializer.descriptor, index, serializer, value
            )
        }
    }

    override fun deserialize(decoder: Decoder): List<Any?> {
        val collectionDecoder = decoder.beginStructure(descriptor) as FirebaseCompositeDecoder
        val list = mutableListOf<Any?>()
        list.forEachIndexed { index, _ ->
            list.add(index, collectionDecoder.decodeNullableSerializableElement(index))
        }
        return list
    }
}

