package dev.gitlive.firebase

import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * Settings used to configure encoding/decoding
 */
sealed class EncodeDecodeSettings {

    /**
     * The [SerializersModule] to use for serialization. This allows for polymorphic serialization on runtime
     */
    abstract val serializersModule: SerializersModule
}

/**
 * [EncodeDecodeSettings] used when encoding an object
 * @property shouldEncodeElementDefault if `true` this will explicitly encode elements even if they are their default value
 * @param serializersModule the [SerializersModule] to use for serialization. This allows for polymorphic serialization on runtime
 */
data class EncodeSettings(
    val shouldEncodeElementDefault: Boolean = true,
    override val serializersModule: SerializersModule = EmptySerializersModule(),
) : EncodeDecodeSettings()

/**
 * [EncodeDecodeSettings] used when decoding an object
 * @param serializersModule the [SerializersModule] to use for deserialization. This allows for polymorphic serialization on runtime
 */
data class DecodeSettings(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
) : EncodeDecodeSettings()
