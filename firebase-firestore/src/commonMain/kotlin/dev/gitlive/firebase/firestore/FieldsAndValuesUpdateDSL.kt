package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.firestore.internal.FieldAndValue
import kotlinx.serialization.SerializationStrategy

/**
 * A builder for generating a collection of updates to a document.
 * Updates can be applied to either a String field or to a [FieldPath].
 * Within this builder custom serialization can be applied to the update.
 *
 * ```
 * val update: FieldsAndValuesUpdateBuilder.() -> Unit = {
 *    buildSettings = { encodeDefaults = false }
 *
 *    "path" to 1
 *    FieldPath("subpath", "field") to "value"
 *    "otherPath".to(strategy, value)
 * }
 * ```
 */
public class FieldsAndValuesUpdateDSL internal constructor() {

    internal val fieldAndValues: MutableList<FieldAndValue> = mutableListOf()

    @PublishedApi
    internal var encodeNextWith: EncodeSettings.Builder.() -> Unit = {
        encodeDefaults = true
    }

    /**
     * Sets the [EncodeSettings.Builder] to apply to the next values added to this update.
     * Updating this value will only influence the encoding of values not yet added to the update.
     * This allows for custom encoding per update, e.g.
     *
     * ```
     * encodeNextWith { encodeDefaults = true }
     * "path" to ClassWithDefaults()
     * encodeNextWith { encodeDefaults = false }
     * "otherPath" to ClassWithDefaults()
     * ```
     */
    public fun encodeNextWith(builder: EncodeSettings.Builder.() -> Unit) {
        encodeNextWith = builder
    }

    /**
     * Updates the field represented by a String to a given value
     * @param T the type of the value
     * @param value the value [T] to update to
     */
    public inline infix fun <reified T> String.to(value: T) {
        toEncoded(encode(value, encodeNextWith))
    }

    /**
     * Updates a [FieldPath] to a given value
     * @param T the type of the value
     * @param value the value [T] to update to
     */
    public inline infix fun <reified T> FieldPath.to(value: T) {
        toEncoded(encode(value, encodeNextWith))
    }

    /**
     * Updates the field represented by a String to a given value
     * @param T the type of the value
     * @param strategy the [SerializationStrategy] to apply to the value
     * @param value the value [T] to update to
     */
    public fun <T : Any> String.to(strategy: SerializationStrategy<T>, value: T) {
        toEncoded(dev.gitlive.firebase.internal.encode(strategy, value, encodeNextWith))
    }

    /**
     * Updates a [FieldPath] to a given value
     * @param T the type of the value
     * @param strategy the [SerializationStrategy] to apply to the value
     * @param value the value [T] to update to
     */
    public fun <T : Any> FieldPath.to(strategy: SerializationStrategy<T>, value: T) {
        toEncoded(dev.gitlive.firebase.internal.encode(strategy, value, encodeNextWith))
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
