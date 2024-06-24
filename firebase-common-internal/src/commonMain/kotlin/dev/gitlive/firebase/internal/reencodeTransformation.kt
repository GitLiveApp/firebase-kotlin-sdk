package dev.gitlive.firebase.internal

import dev.gitlive.firebase.EncodeDecodeSettingsBuilder
import kotlinx.serialization.KSerializer

public inline fun <reified T> reencodeTransformation(value: Any?, builder: EncodeDecodeSettingsBuilder.() -> Unit = {}, transform: (T) -> T): Any? {
    val encodeDecodeSettingsBuilder = EncodeDecodeSettingsBuilderImpl().apply(builder)
    val oldValue: T = decode(value, encodeDecodeSettingsBuilder.buildDecodeSettings())
    return encode(
        transform(oldValue),
        encodeDecodeSettingsBuilder.buildEncodeSettings(),
    )
}

public inline fun <T> reencodeTransformation(strategy: KSerializer<T>, value: Any?, builder: EncodeDecodeSettingsBuilder.() -> Unit = {}, transform: (T) -> T): Any? {
    val encodeDecodeSettingsBuilder = EncodeDecodeSettingsBuilderImpl().apply(builder)
    val oldValue: T = decode(strategy, value, encodeDecodeSettingsBuilder.buildDecodeSettings())
    return encode(
        strategy,
        transform(oldValue),
        encodeDecodeSettingsBuilder.buildEncodeSettings(),
    )
}
