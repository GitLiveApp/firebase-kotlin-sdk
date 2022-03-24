package dev.gitlive.firebase

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.findPolymorphicSerializer
import kotlinx.serialization.internal.AbstractPolymorphicSerializer


@Suppress("UNCHECKED_CAST")
internal fun <T> FirebaseEncoder.encodePolymorphically(
    serializer: SerializationStrategy<T>,
    value: T,
    ifPolymorphic: (String) -> Unit
) {
    if (serializer !is AbstractPolymorphicSerializer<*>) {
        serializer.serialize(this, value)
        return
    }
    val casted = serializer as AbstractPolymorphicSerializer<Any>
    val baseClassDiscriminator = serializer.descriptor.classDiscriminator()
    val actualSerializer = casted.findPolymorphicSerializer(this, value as Any)
//    validateIfSealed(casted, actualSerializer, baseClassDiscriminator)
//    checkKind(actualSerializer.descriptor.kind)
    ifPolymorphic(baseClassDiscriminator)
    actualSerializer.serialize(this, value)
}



@Suppress("UNCHECKED_CAST")
internal fun <T> FirebaseDecoder.decodeSerializableValuePolymorphic(
    value: Any?,
    decodeDouble: (value: Any?) -> Double?,
    deserializer: DeserializationStrategy<T>,
): T {
    if (deserializer !is AbstractPolymorphicSerializer<*>) {
        return deserializer.deserialize(this)
    }

    val casted = deserializer as AbstractPolymorphicSerializer<Any>
    val discriminator = deserializer.descriptor.classDiscriminator()
    val type = getPolymorphicType(value, discriminator)
    val actualDeserializer = casted.findPolymorphicSerializerOrNull(
        structureDecoder(deserializer.descriptor, decodeDouble),
        type
    ) as DeserializationStrategy<T>
    return actualDeserializer.deserialize(this)
}


//private fun validateIfSealed(
//    serializer: SerializationStrategy<*>,
//    actualSerializer: SerializationStrategy<Any>,
//    classDiscriminator: String
//) {
//    if (serializer !is SealedClassSerializer<*>) return
//    @Suppress("DEPRECATION_ERROR")
//    if (classDiscriminator in actualSerializer.descriptor.jsonCachedSerialNames()) {
//        val baseName = serializer.descriptor.serialName
//        val actualName = actualSerializer.descriptor.serialName
//        error(
//            "Sealed class '$actualName' cannot be serialized as base class '$baseName' because" +
//                    " it has property name that conflicts with class discriminator '$classDiscriminator'. " +
//                    "You can either change class discriminator with FirebaseClassDiscriminator annotation or " +
//                    "rename property with @SerialName annotation"
//        )
//    }
//}

//internal fun checkKind(kind: SerialKind) {
//    if (kind is SerialKind.ENUM) error("Enums cannot be serialized polymorphically with 'type' parameter. You can use 'JsonBuilder.useArrayPolymorphism' instead")
//    if (kind is PrimitiveKind) error("Primitives cannot be serialized polymorphically with 'type' parameter. You can use 'JsonBuilder.useArrayPolymorphism' instead")
//    if (kind is PolymorphicKind) error("Actual serializer for polymorphic cannot be polymorphic itself")
//}

internal fun SerialDescriptor.classDiscriminator(): String {
    // Plain loop is faster than allocation of Sequence or ArrayList
    // We can rely on the fact that only one FirebaseClassDiscriminator is present —
    // compiler plugin checked that.
    for (annotation in annotations) {
        if (annotation is FirebaseClassDiscriminator) return annotation.discriminator
    }
    return "type"
}

