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

    interface Builder {
        var shouldEncodeElementDefault: Boolean
        var serializersModule: SerializersModule

    }

    @PublishedApi
    internal class BuilderImpl : Builder {
        override var shouldEncodeElementDefault: Boolean = true
        override var serializersModule: SerializersModule = EmptySerializersModule()
    }
}

/**
 * [EncodeDecodeSettings] used when decoding an object
 * @param serializersModule the [SerializersModule] to use for deserialization. This allows for polymorphic serialization on runtime
 */
data class DecodeSettings internal constructor(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
) : EncodeDecodeSettings() {

    interface Builder {
        var serializersModule: SerializersModule
    }

    @PublishedApi
    internal class BuilderImpl : Builder {
        override var serializersModule: SerializersModule = EmptySerializersModule()
    }
}

interface EncodeDecodeSettingsBuilder : EncodeSettings.Builder, DecodeSettings.Builder

@PublishedApi
internal class EncodeDecodeSettingsBuilderImpl : EncodeDecodeSettingsBuilder {

    override var shouldEncodeElementDefault: Boolean = true
    override var serializersModule: SerializersModule = EmptySerializersModule()
}

@PublishedApi
internal fun EncodeSettings.Builder.buildEncodeSettings(): EncodeSettings = EncodeSettings(shouldEncodeElementDefault, serializersModule)
@PublishedApi
internal fun DecodeSettings.Builder.buildDecodeSettings(): DecodeSettings = DecodeSettings(serializersModule)
