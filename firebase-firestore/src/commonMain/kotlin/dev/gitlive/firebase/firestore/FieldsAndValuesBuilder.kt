package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.EncodeSettings
import kotlinx.serialization.SerializationStrategy

public abstract class FieldsAndValuesBuilder {

    public var buildSettings: EncodeSettings.Builder.() -> Unit = {
        encodeDefaults = true
    }

    public inline infix fun <reified T> String.to(value: T) {
        toEncoded(encode(value, buildSettings))
    }

    public inline infix fun <reified T> FieldPath.to(value: T) {
        toEncoded(encode(value, buildSettings))
    }

    public fun <T : Any> String.to(strategy: SerializationStrategy<T>, value: T) {
        toEncoded(dev.gitlive.firebase.internal.encode(strategy, value, buildSettings))
    }

    public fun <T : Any> FieldPath.to(strategy: SerializationStrategy<T>, value: T) {
        toEncoded(dev.gitlive.firebase.internal.encode(strategy, value, buildSettings))
    }

    @PublishedApi
    internal abstract fun String.toEncoded(encodedValue: Any?)

    @PublishedApi
    internal abstract fun FieldPath.toEncoded(encodedValue: Any?)
}
