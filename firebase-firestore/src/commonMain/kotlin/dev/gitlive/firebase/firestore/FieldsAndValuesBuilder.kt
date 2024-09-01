package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.firestore.internal.FieldAndValue
import kotlinx.serialization.SerializationStrategy

public class FieldsAndValuesBuilder internal constructor() {

    internal val fieldAndValues: MutableList<FieldAndValue> = mutableListOf()
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
    internal fun String.toEncoded(encodedValue: Any?) {
        fieldAndValues += FieldAndValue.WithStringField(this, encodedValue)
    }

    @PublishedApi
    internal fun FieldPath.toEncoded(encodedValue: Any?) {
        fieldAndValues += FieldAndValue.WithFieldPath(this, encodedValue)
    }
}
