package dev.gitlive.firebase

import dev.gitlive.firebase.EncodeDecodeSettings.PolymorphicStructure
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * Settings used to configure encoding/decoding
 */
sealed class EncodeDecodeSettings {

    /**
     * The structure in which Polymorphic classes are to be serialized
     */
    enum class PolymorphicStructure {

        /**
         * A [PolymorphicStructure] where the polymorphic class is serialized as a Map, with a key for `type` reserved for the polymorphic discriminator
         */
        MAP,

        /**
         * A [PolymorphicStructure] where the polymorphic class is serialized as a List, with the polymorphic discriminator as its first element and the serialized object as its second element
         */
        LIST
    }

    /**
     * The [SerializersModule] to use for serialization. This allows for polymorphic serialization on runtime
     */
    abstract val serializersModule: SerializersModule

    /**
     * The [PolymorphicStructure] to use for encoding/decoding polymorphic classes
     */
    abstract val polymorphicStructure: PolymorphicStructure
}

/**
 * [EncodeDecodeSettings] used when encoding an object
 * @property shouldEncodeElementDefault if `true` this will explicitly encode elements even if they are their default value
 * @param serializersModule the [SerializersModule] to use for serialization. This allows for polymorphic serialization on runtime
 * @param polymorphicStructure the [PolymorphicStructure] to use for encoding polymorphic classes
 */
data class EncodeSettings(
    val shouldEncodeElementDefault: Boolean = true,
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    override val polymorphicStructure: PolymorphicStructure = PolymorphicStructure.MAP
) : EncodeDecodeSettings()

/**
 * [EncodeDecodeSettings] used when decoding an object
 * @param serializersModule the [SerializersModule] to use for deserialization. This allows for polymorphic serialization on runtime
 * @param polymorphicStructure the [PolymorphicStructure] to use for decoding polymorphic classes
 */
data class DecodeSettings(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    override val polymorphicStructure: PolymorphicStructure = PolymorphicStructure.MAP
) : EncodeDecodeSettings()