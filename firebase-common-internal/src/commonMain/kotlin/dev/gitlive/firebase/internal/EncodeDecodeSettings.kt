package dev.gitlive.firebase.internal

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeDecodeSettingsBuilder
import dev.gitlive.firebase.EncodeSettings
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@PublishedApi
internal data class EncodeSettingsImpl internal constructor(
    override val encodeDefaults: Boolean,
    override val serializersModule: SerializersModule,
) : EncodeSettings {

    @PublishedApi
    internal class Builder : EncodeSettings.Builder {
        override var encodeDefaults: Boolean = true
        override var serializersModule: SerializersModule = EmptySerializersModule()
    }
}

@PublishedApi
internal class DecodeSettingsImpl internal constructor(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
) : DecodeSettings {

    @PublishedApi
    internal class Builder : DecodeSettings.Builder {
        override var serializersModule: SerializersModule = EmptySerializersModule()
    }
}

@PublishedApi
internal class EncodeDecodeSettingsBuilderImpl : EncodeDecodeSettingsBuilder {

    override var encodeDefaults: Boolean = true
    override var serializersModule: SerializersModule = EmptySerializersModule()
}

@PublishedApi
internal fun EncodeSettings.Builder.buildEncodeSettings(): EncodeSettings = EncodeSettingsImpl(encodeDefaults, serializersModule)

@PublishedApi
internal fun DecodeSettings.Builder.buildDecodeSettings(): DecodeSettings = DecodeSettingsImpl(serializersModule)
