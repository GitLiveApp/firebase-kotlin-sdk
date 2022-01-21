package dev.gitlive.firebase.firestore

import kotlinx.serialization.SerializationStrategy

expect fun isFieldValue(value: Any) : Boolean

@PublishedApi
internal inline fun <reified T> encode(value: T, shouldEncodeElementDefault: Boolean) =
    if (value?.let(::isFieldValue) == true) {
        value
    } else {
        dev.gitlive.firebase.encode(value, shouldEncodeElementDefault, FieldValue.serverTimestamp())
    }

@PublishedApi
internal fun <T> encode(strategy: SerializationStrategy<T>, value: T, shouldEncodeElementDefault: Boolean): Any? =
    dev.gitlive.firebase.encode(
        strategy,
        value,
        shouldEncodeElementDefault,
        FieldValue.serverTimestamp()
    )

@PublishedApi
internal fun <T> encodeAsMap(
    strategy: SerializationStrategy<T>,
    data: T,
    encodeDefaults: Boolean = false
): Map<String, Any?> = encode(strategy, data, encodeDefaults) as Map<String, Any?>

@PublishedApi
internal fun encodeAsMap(
    encodeDefaults: Boolean = false,
    vararg fieldsAndValues: Pair<String, Any?>
): Map<String, Any?>? = fieldsAndValues.takeUnless { fieldsAndValues.isEmpty() }
    ?.map { (field, value) -> field to encode(value, encodeDefaults) }?.toMap()
