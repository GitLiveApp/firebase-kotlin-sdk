package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.EncodeSettings
import kotlinx.serialization.SerializationStrategy

/**
 * A builder for generating the field values of a [Query].
 * The order of the field values must match the order by clauses of the [Query]
 */
public class FieldValuesDSL internal constructor() {

    internal val fieldValues: MutableList<Any> = mutableListOf()

    @PublishedApi
    internal var encodeNextWith: EncodeSettings.Builder.() -> Unit = {
        encodeDefaults = true
    }

    /**
     * Sets the [EncodeSettings.Builder] to apply to the next field values added.
     * Updating this value will only influence the encoding of field values not yet added to the update.
     * This allows for custom encoding per value, e.g.
     *
     * ```
     * encodeNextWith { encodeDefaults = true }
     * add(ClassWithDefaults())
     * encodeNextWith { encodeDefaults = false }
     * add(ClassWithDefaults())
     * ```
     */
    public fun encodeNextWith(builder: EncodeSettings.Builder.() -> Unit) {
        encodeNextWith = builder
    }

    /**
     * Adds a field value to the [Query]
     * @param T the type of the value to add
     * @param value the value [T] to add
     */
    public inline fun <reified T> add(value: T) {
        addEncoded(encode(value, encodeNextWith)!!)
    }

    /**
     * Adds a field value to the [Query]
     * @param T the type of the value to add
     * @param strategy the [SerializationStrategy] to apply to the value
     * @param value the value [T] to add
     */
    public fun <T : Any> addWithStrategy(strategy: SerializationStrategy<T>, value: T) {
        addEncoded(dev.gitlive.firebase.internal.encode(strategy, value, encodeNextWith)!!)
    }

    @PublishedApi
    internal fun addEncoded(encodedValue: Any) {
        fieldValues += encodedValue
    }
}
