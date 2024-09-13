package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.EncodeSettings
import kotlinx.serialization.SerializationStrategy

public abstract class FieldValueBuilder internal constructor() {

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
    internal abstract fun addEncoded(encodedValue: Any)
}
