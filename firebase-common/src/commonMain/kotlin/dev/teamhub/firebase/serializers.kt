package dev.teamhub.firebase

import kotlinx.serialization.*
import kotlinx.serialization.builtins.UnitSerializer
import kotlinx.serialization.builtins.nullable

@ImplicitReflectionSerializer
@Suppress("UNCHECKED_CAST")
fun Any.firebaseSerializer() = (this::class.serializerOrNull() ?: this::class.serializerOrNull() ?: when(this) {
    is Map<*, *> -> FirebaseMapSerializer()
    is List<*> -> FirebaseListSerializer()
    is Set<*> -> FirebaseListSerializer()
    else -> throw SerializationException("Can't locate argument-less serializer for $this. For generic classes, such as lists, please provide serializer explicitly.")
}) as KSerializer<Any>

@ImplicitReflectionSerializer
class FirebaseMapSerializer : KSerializer<Map<String, Any?>> {

    lateinit var keys: List<String>
    lateinit var map: Map<String, Any?>

    override val descriptor = object : SerialDescriptor {
        override val kind = StructureKind.MAP
        override val serialName: String = "kotlin.Map<String, Any>"
        override fun getElementAnnotations(index: Int) = throw NotImplementedError()
        override fun getElementDescriptor(index: Int) = throw NotImplementedError()
        override fun getElementIndex(name: String) = keys.indexOf(name)
        override fun getElementName(index: Int) = keys[index]
        override fun isElementOptional(index: Int) = keys.getOrNull(index).isNullOrEmpty()

        override val elementsCount get() = map.size
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(encoder: Encoder, value: Map<String, Any?>) {
        map = value
        keys = value.keys.toList()
        val collectionEncoder = encoder.beginCollection(descriptor, value.size)
        keys.forEachIndexed { index, key ->
            val itemValue = map.getValue(key)
            val serializer = itemValue?.firebaseSerializer() ?: UnitSerializer().nullable as KSerializer<Any>
            collectionEncoder.encodeNullableSerializableElement(
                serializer.descriptor, index, serializer, itemValue
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

@ImplicitReflectionSerializer
class FirebaseListSerializer : KSerializer<Iterable<Any?>> {

    lateinit var list: List<Any?>

    override val descriptor= object : SerialDescriptor {
        override val kind = StructureKind.LIST
        override val serialName = "kotlin.List<Any>"
        override fun getElementAnnotations(index: Int) = throw NotImplementedError()
        override fun getElementDescriptor(index: Int) = throw NotImplementedError()
        override fun getElementIndex(name: String) = throw NotImplementedError()
        override fun getElementName(index: Int) = throw NotImplementedError()
        override fun isElementOptional(index: Int) = throw NotImplementedError()

        override val elementsCount get() = list.size
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(encoder: Encoder, value: Iterable<Any?>) {
        list = value.toList()
        val collectionEncoder = encoder.beginCollection(descriptor, list.size)
        list.forEachIndexed { index, item ->
            val serializer = item?.firebaseSerializer() ?: UnitSerializer().nullable as KSerializer<Any>
            collectionEncoder.encodeNullableSerializableElement(
                serializer.descriptor, index, serializer, item
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
