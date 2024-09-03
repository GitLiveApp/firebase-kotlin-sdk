package dev.gitlive.firebase.internal

import kotlin.jvm.JvmInline

/**
 * Platform specific object for storing encoded data that can be used for methods that explicitly require an object.
 * This is essentially a [Map] of [String] and [Any]? (as represented by [raw]) but since [encode] gives a platform specific value, this method wraps that.
 */
public sealed interface EncodedObject

internal fun EncodedObject.getRaw(): Map<String, Any?> = when (this) {
    is EncodedObjectImpl -> raw
}

@JvmInline
@PublishedApi
internal value class EncodedObjectImpl(val raw: Map<String, Any?>) : EncodedObject

@PublishedApi
internal expect fun Any.asNativeMap(): Map<*, *>?

@PublishedApi
internal fun Map<*, *>.asEncodedObject(): EncodedObject = map { (key, value) ->
    if (key is String) {
        key to value
    } else {
        throw IllegalArgumentException("Expected a String key but received $key")
    }
}.toMap().let(::EncodedObjectImpl)
