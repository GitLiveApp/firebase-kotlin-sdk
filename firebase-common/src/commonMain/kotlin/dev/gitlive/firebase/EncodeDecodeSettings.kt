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
data class EncodeSettings internal constructor(
    val shouldEncodeElementDefault: Boolean,
    override val serializersModule: SerializersModule,
) : EncodeDecodeSettings() {
    class Builder {
        var shouldEncodeElementDefault: Boolean = true
        var serializersModule: SerializersModule = EmptySerializersModule()

        @PublishedApi
        internal fun build() = EncodeSettings(shouldEncodeElementDefault, serializersModule)
    }
}

/**
 * [EncodeDecodeSettings] used when decoding an object
 * @param serializersModule the [SerializersModule] to use for deserialization. This allows for polymorphic serialization on runtime
 */
data class DecodeSettings internal constructor(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
) : EncodeDecodeSettings() {

    class Builder {
        var serializersModule: SerializersModule = EmptySerializersModule()

        fun build() = DecodeSettings(serializersModule)
    }
}
