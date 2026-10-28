/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlin.js.json

/*
 * The JS SDK returns the values JSON.parse produces: primitives, which are Kotlin's own on Kotlin/JS, plain objects and
 * arrays. A Kotlin Array is a JS array too, so on this platform convertToPlainValue reads an Array as a List instead of
 * rejecting it as the Android SDK does; the serializer of an Array property still rejects it.
 */

internal actual fun platformValueToPlain(value: Any): Any? = when {
    isJsArray(value) -> value.unsafeCast<Array<Any?>>().map { it.undefinedToNull() }
    isPlainJsObject(value) -> LinkedHashMap<String, Any?>().also { map ->
        for (entry in objectEntries(value)) map[entry[0] as String] = entry[1].undefinedToNull()
    }
    else -> value
}

internal actual fun plainValueToPlatform(value: Any?): Any? = when (value) {
    is Map<*, *> -> json(*value.entries.map { (key, element) -> key.toString() to plainValueToPlatform(element) }.toTypedArray())
    is Collection<*> -> value.map { plainValueToPlatform(it) }.toTypedArray()
    is Long -> value.toDouble()
    else -> value
}

private fun isJsArray(value: Any): Boolean = js("Array.isArray(value)").unsafeCast<Boolean>()

private fun isPlainJsObject(value: Any): Boolean = jsTypeOf(value) == "object" && js("Object.getPrototypeOf(value) === Object.prototype").unsafeCast<Boolean>()

private fun objectEntries(value: Any): Array<Array<Any?>> = js("Object.entries(value)").unsafeCast<Array<Array<Any?>>>()

private fun Any?.undefinedToNull(): Any? = if (this === undefined) null else this
