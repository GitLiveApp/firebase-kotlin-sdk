/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlin.reflect.KClass

/** The Java name of a class, as the Android SDK's messages print it (`java.util.HashMap`, `java.lang.String`). */
internal expect fun javaClassNameOf(type: KClass<*>): String

/** Logs a warning as the Android SDK's mappers do, with `android.util.Log.w` on Android. */
internal expect fun logWarning(tag: String, message: String)

/**
 * A map-like plain value as the platform's Firebase SDK exchanges it: a Kotlin [Map] on Android, the JVM and Apple
 * platforms, a JS object on JS and wasmJs, where a Kotlin [Map] is read too. The encoder builds one with the constructor
 * and hands out its [value]; the decoder reads one through [of].
 */
internal expect class PlainMap() {
    /** The platform's value. */
    val value: Any
    val size: Int
    val keys: List<String>
    fun containsKey(key: String): Boolean

    /** The element as the platform holds it; [asRead] makes it the value the decoder takes. */
    operator fun get(key: String): Any?
    operator fun set(key: String, element: Any?)

    companion object {
        /** [value] read as a map, or `null` when it is not one. */
        fun of(value: Any): PlainMap?
    }
}

/** A list-like plain value, a Kotlin [List] or, on JS and wasmJs, a JS array; see [PlainMap]. */
internal expect class PlainList() {
    val value: Any
    val size: Int
    operator fun get(index: Int): Any?

    /** Sets the element at [index], extending the list with `null`s up to it. */
    operator fun set(index: Int, element: Any?)

    companion object {
        fun of(value: Any): PlainList?
    }
}

/**
 * A leaf of the platform's Firebase SDK as the decoder reads it: the Kotlin counterpart of a primitive (the JS SDK's
 * primitives on wasmJs; a JS `undefined` and Foundation's `NSNull` are `null`; a Foundation boolean is a `Boolean`),
 * and any other value as it is.
 */
internal expect fun platformLeaf(value: Any): Any?

/**
 * The platform SDK's representation of a leaf the format produced: on JS a JS number for a [Long], which is a Kotlin
 * object there; on wasmJs the JS value of a Kotlin primitive or string; on Apple a Foundation boolean for a [Boolean];
 * the value itself elsewhere. The writers of [PlainMap] and [PlainList] apply it to every element, and the format
 * applies it to a root value.
 */
internal expect fun toPlatformLeaf(value: Any?): Any?

/**
 * Whether the platform's Cloud Functions SDK encodes a [Long] losslessly itself: the Android, JVM and iOS SDKs wrap one
 * as a protobuf `Int64Value`, the JS SDK sends a number, so the format writes the wrapper there (see [int64Value]).
 */
internal expect val functionsSdkEncodesLongs: Boolean

/** The protobuf `Int64Value` JSON the callable protocol takes for a 64-bit integer. */
internal fun int64Value(value: Long): Any = PlainMap().also {
    it["@type"] = "type.googleapis.com/google.protobuf.Int64Value"
    it["value"] = value.toString()
}.value

/** [platformLeaf] for any value; Kotlin primitives, the usual leaves, skip the platform check. */
internal fun Any?.asRead(): Any? = when (this) {
    null, is String, is Number, is Boolean -> this
    else -> platformLeaf(this)
}

/**
 * The Kotlin value of a plain value, for a value read into `Any`: a map-like value becomes a [Map] and a list-like value
 * a [List], recursively, with the leaves [asRead] and, given [rules], read through [Rules.readValue]; a value that
 * already is a Kotlin value is returned as it is.
 */
internal fun toKotlin(value: Any?, rules: Rules? = null): Any? {
    val read = value.asRead() ?: return null
    PlainMap.of(read)?.let { map ->
        var changed = read !is Map<*, *>
        val result = LinkedHashMap<String, Any?>(map.size)
        for (key in map.keys) {
            val element = map[key]
            result[key] = toKotlin(element, rules).also { if (it !== element) changed = true }
        }
        return if (changed) result else read
    }
    PlainList.of(read)?.let { list ->
        var changed = read !is List<*>
        val result = ArrayList<Any?>(list.size)
        for (index in 0 until list.size) {
            val element = list[index]
            result.add(toKotlin(element, rules).also { if (it !== element) changed = true })
        }
        return if (changed) result else read
    }
    return if (rules == null || read is String || read is Number || read is Boolean) read else rules.readValue(read)
}
