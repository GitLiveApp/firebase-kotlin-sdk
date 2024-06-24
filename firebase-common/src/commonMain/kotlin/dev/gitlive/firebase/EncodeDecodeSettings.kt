package dev.gitlive.firebase

import kotlinx.serialization.modules.SerializersModule

/**
 * Settings used to configure encoding/decoding
 */
sealed interface EncodeDecodeSettings {

    /**
     * The [SerializersModule] to use for serialization. This allows for polymorphic serialization on runtime
     */
    val serializersModule: SerializersModule
}

/**
 * [EncodeDecodeSettings] used when encoding an object
 * @property encodeDefaults if `true` this will explicitly encode elements even if they are their default value
 */
interface EncodeSettings : EncodeDecodeSettings {

    val encodeDefaults: Boolean

    interface Builder {
        var encodeDefaults: Boolean
        var serializersModule: SerializersModule
    }
}

/**
 * [EncodeDecodeSettings] used when decoding an object
 * @param serializersModule the [SerializersModule] to use for deserialization. This allows for polymorphic serialization on runtime
 */
interface DecodeSettings : EncodeDecodeSettings {

    interface Builder {
        var serializersModule: SerializersModule
    }
}

interface EncodeDecodeSettingsBuilder :
    EncodeSettings.Builder,
    DecodeSettings.Builder
