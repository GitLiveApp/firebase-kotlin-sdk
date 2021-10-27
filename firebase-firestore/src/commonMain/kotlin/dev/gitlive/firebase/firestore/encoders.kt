package dev.gitlive.firebase.firestore

import kotlinx.serialization.SerializationStrategy

expect fun isSpecialValue(value: Any) : Boolean

@PublishedApi
internal inline fun <reified T> encode(value: T, shouldEncodeElementDefault: Boolean) =
    if (value?.let(::isSpecialValue) == true) {
        value
    } else {
        dev.gitlive.firebase.encode(value, shouldEncodeElementDefault, FieldValue.serverTimestamp())
    }

internal fun <T> encode(strategy: SerializationStrategy<T>, value: T, shouldEncodeElementDefault: Boolean): Any? =
    dev.gitlive.firebase.encode(
        strategy,
        value,
        shouldEncodeElementDefault,
        FieldValue.serverTimestamp()
    )
