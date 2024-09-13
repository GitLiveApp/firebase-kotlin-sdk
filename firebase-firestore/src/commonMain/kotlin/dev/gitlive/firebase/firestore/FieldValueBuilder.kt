package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.EncodeSettings
import kotlinx.serialization.SerializationStrategy

public class FieldValueBuilder internal constructor() {

    internal val fieldValues: MutableList<Any> = mutableListOf()
    public var buildSettings: EncodeSettings.Builder.() -> Unit = {
        encodeDefaults = true
    }

    public inline fun <reified T> add(value: T) {
        addEncoded(encode(value, buildSettings)!!)
    }

    public fun <T : Any> addWithStrategy(strategy: SerializationStrategy<T>, value: T) {
        addEncoded(dev.gitlive.firebase.internal.encode(strategy, value, buildSettings)!!)
    }

    @PublishedApi
    internal fun addEncoded(encodedValue: Any) {
        fieldValues += encodedValue
    }
}
