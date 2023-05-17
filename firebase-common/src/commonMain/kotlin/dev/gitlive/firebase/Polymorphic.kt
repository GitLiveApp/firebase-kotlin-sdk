package dev.gitlive.firebase

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.findPolymorphicSerializer
import kotlinx.serialization.internal.AbstractPolymorphicSerializer

/*
 * This code was inspired on polymorphic json serialization of kotlinx.serialization.
 * See https://github.com/Kotlin/kotlinx.serialization/blob/master/formats/json/commonMain/src/kotlinx/serialization/json/internal/Polymorphic.kt
 */
@Suppress("UNCHECKED_CAST")
internal fun <T> FirebaseEncoder.encodePolymorphically(
    serializer: SerializationStrategy<T>,
    value: T,
    settings: EncodeSettings,
    ifPolymorphic: (String) -> Unit
) {
    // If serializer is not an AbstractPolymorphicSerializer or if we are encoding this as a list, we can just use the regular serializer
    // This will result in calling structureEncoder for complicated structures
    // For PolymorphicKind this will first encode the polymorphic discriminator as a String and the remaining StructureKind.Class as a map of key-value pairs
    // This will result in a list structured like: (type, { classKey = classValue })
    if (serializer !is AbstractPolymorphicSerializer<*> || settings.polymorphicStructure == EncodeDecodeSettings.PolymorphicStructure.LIST) {
        serializer.serialize(this, value)
        return
    }

    // When doing Polymorphic Serialization with EncodeDecodeSettings.PolymorphicStructure.MAP we will use the polymorphic serializer of the class.
    val casted = serializer as AbstractPolymorphicSerializer<Any>
    val baseClassDiscriminator = serializer.descriptor.classDiscriminator()
    val actualSerializer = casted.findPolymorphicSerializer(this, value as Any)
    ifPolymorphic(baseClassDiscriminator)
    actualSerializer.serialize(this, value)
}

@Suppress("UNCHECKED_CAST")
internal fun <T> FirebaseDecoder.decodeSerializableValuePolymorphic(
    value: Any?,
    decodeSettings: DecodeSettings,
    deserializer: DeserializationStrategy<T>,
): T {
    // If deserializer is not an AbstractPolymorphicSerializer or if we are decoding this from a list, we can just use the regular serializer
    if (deserializer !is AbstractPolymorphicSerializer<*> || decodeSettings.polymorphicStructure == EncodeDecodeSettings.PolymorphicStructure.LIST) {
        return deserializer.deserialize(this)
    }
    val casted = deserializer as AbstractPolymorphicSerializer<Any>
    val discriminator = deserializer.descriptor.classDiscriminator()
    val type = getPolymorphicType(value, discriminator)
    val actualDeserializer = casted.findPolymorphicSerializerOrNull(
        structureDecoder(deserializer.descriptor, false),
        type
    ) as DeserializationStrategy<T>
    return actualDeserializer.deserialize(this)
}

internal fun SerialDescriptor.classDiscriminator(): String {
    // Plain loop is faster than allocation of Sequence or ArrayList
    // We can rely on the fact that only one FirebaseClassDiscriminator is present —
    // compiler plugin checked that.
    for (annotation in annotations) {
        if (annotation is FirebaseClassDiscriminator) return annotation.discriminator
    }
    return "type"
}

