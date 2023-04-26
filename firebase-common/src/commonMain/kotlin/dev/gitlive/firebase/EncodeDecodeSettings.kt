package dev.gitlive.firebase

import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

sealed class EncodeDecodeSettings {

    enum class PolymorphicStructure {
        MAP,
        LIST
    }

    abstract val serializersModule: SerializersModule
    abstract val polymorphicStructure: PolymorphicStructure
}

data class EncodeSettings(
    val shouldEncodeElementDefault: Boolean,
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    override val polymorphicStructure: PolymorphicStructure = PolymorphicStructure.MAP
) : EncodeDecodeSettings()

data class DecodeSettings(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    override val polymorphicStructure: PolymorphicStructure = PolymorphicStructure.MAP
) : EncodeDecodeSettings()