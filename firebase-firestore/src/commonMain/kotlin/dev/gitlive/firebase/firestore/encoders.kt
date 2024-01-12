package dev.gitlive.firebase.firestore

import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/** @return whether value is special and shouldn't be encoded/decoded. */
@PublishedApi
internal expect fun isSpecialValue(value: Any): Boolean

@PublishedApi
internal inline fun <reified T> encode(value: T, encodeDefaults: Boolean = true, serializersModule: SerializersModule = EmptySerializersModule()) =
    if (value?.let(::isSpecialValue) == true) {
        value
    } else {
        dev.gitlive.firebase.encode(value, encodeDefaults, serializersModule)
    }
