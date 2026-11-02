/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

/*
 * The JS SDK exchanges the values JSON.parse produces and JSON.stringify takes: primitives, which are Kotlin's own on
 * Kotlin/JS, plain objects and arrays. A Kotlin Array is a JS array too, so on this platform convertToPlainValue reads an
 * Array as a list instead of rejecting it as the Android SDK does; the serializer of an Array property still rejects it.
 */

internal actual class PlainMap private constructor(private val map: Map<*, *>?, private val js: dynamic) {
    actual constructor() : this(null, js("({})"))

    actual val value: Any get() = map ?: js.unsafeCast<Any>()
    actual val size: Int get() = map?.size ?: keysOf(js).size
    actual val keys: List<String> get() = map?.keys?.map { it.toString() } ?: keysOf(js).asList()

    actual fun containsKey(key: String): Boolean = map?.containsKey(key) ?: hasOwn(js, key)

    actual operator fun get(key: String): Any? = if (map != null) map[key] else js[key].unsafeCast<Any?>()

    actual operator fun set(key: String, element: Any?) {
        define(js, key, toPlatformLeaf(element))
    }

    actual companion object {
        actual fun of(value: Any): PlainMap? = when {
            value is Map<*, *> -> PlainMap(value, null)
            isPlainJsObject(value) -> PlainMap(null, value)
            else -> null
        }
    }
}

internal actual class PlainList private constructor(private val list: List<*>?, private val js: dynamic) {
    actual constructor() : this(null, js("([])"))

    actual val value: Any get() = list ?: js.unsafeCast<Any>()
    actual val size: Int get() = list?.size ?: js.length.unsafeCast<Int>()

    actual operator fun get(index: Int): Any? = if (list != null) list[index] else js[index].unsafeCast<Any?>()

    actual operator fun set(index: Int, element: Any?) {
        js[index] = toPlatformLeaf(element)
    }

    actual companion object {
        actual fun of(value: Any): PlainList? = when {
            value is List<*> -> PlainList(value, null)
            isJsArray(value) -> PlainList(null, value)
            else -> null
        }
    }
}

internal actual fun platformLeaf(value: Any): Any? = if (value === undefined) null else value

/** A JS number for a Kotlin Long, which is a Kotlin object on Kotlin/JS; everything else is its own JS value. */
internal actual fun toPlatformLeaf(value: Any?): Any? = if (value is Long) value.toDouble() else value

internal actual val functionsSdkEncodesLongs: Boolean = false

private fun isJsArray(value: Any): Boolean = js("Array.isArray(value)").unsafeCast<Boolean>()

private fun isPlainJsObject(value: Any): Boolean = jsTypeOf(value) == "object" && js("Object.getPrototypeOf(value) === Object.prototype").unsafeCast<Boolean>()

private fun keysOf(target: dynamic): Array<String> = js("Object.keys(target)").unsafeCast<Array<String>>()

private fun hasOwn(target: dynamic, key: String): Boolean = js("Object.prototype.hasOwnProperty.call(target, key)").unsafeCast<Boolean>()

/** An own data property, whatever the key: `target[key] = value` would run an inherited setter for `__proto__`. */
private fun define(target: dynamic, key: String, value: Any?) {
    js("Object.defineProperty(target, key, { value: value, enumerable: true, writable: true, configurable: true })")
}
