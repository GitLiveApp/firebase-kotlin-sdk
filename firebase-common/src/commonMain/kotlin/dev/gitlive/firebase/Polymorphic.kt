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
    ifPolymorphic: (String) -> Unit
) {
    if (serializer !is AbstractPolymorphicSerializer<*>) {
        serializer.serialize(this, value)
        return
    }
    val casted = serializer as AbstractPolymorphicSerializer<Any>
    val baseClassDiscriminator = serializer.descriptor.classDiscriminator()
    val actualSerializer = casted.findPolymorphicSerializer(this, value as Any)
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

internal fun SerialDescriptor.classDiscriminator(): String {
    // Plain loop is faster than allocation of Sequence or ArrayList
    // We can rely on the fact that only one FirebaseClassDiscriminator is present —
    // compiler plugin checked that.
    for (annotation in annotations) {
        if (annotation is FirebaseClassDiscriminator) return annotation.discriminator
    }
    return "type"
}

