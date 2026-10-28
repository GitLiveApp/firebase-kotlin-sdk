/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.jsIsArray
import dev.gitlive.firebase.externals.jsObject
import dev.gitlive.firebase.externals.jsObjectKeys
import dev.gitlive.firebase.externals.jsTypeOf
import dev.gitlive.firebase.externals.toJs
import dev.gitlive.firebase.externals.toKotlin
import dev.gitlive.firebase.externals.toKotlinString

/*
 * The JS SDK exchanges the values JSON.parse produces and JSON.stringify takes, held as JsAny. A Kotlin value cast to
 * JsAny is an opaque reference on the JS side (typeof "object", no prototype), so the JS-side checks are false for it.
 */

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual class PlainMap private constructor(private val map: Map<*, *>?, private val js: JsAny?) {
    actual constructor() : this(null, jsObject())

    actual val value: Any get() = map ?: js!!
    actual val size: Int get() = map?.size ?: jsObjectKeys(js!!).length
    actual val keys: List<String> get() = map?.keys?.map { it.toString() } ?: jsObjectKeys(js!!).let { keys -> List(keys.length) { keys[it]!!.toKotlinString() } }

    actual fun containsKey(key: String): Boolean = map?.containsKey(key) ?: jsHasOwn(js!!, key)

    actual operator fun get(key: String): Any? = if (map != null) map[key] else jsGet(js!!, key)

    actual operator fun set(key: String, element: Any?) {
        jsDefine(js!!, key, element.toJs())
    }

    actual companion object {
        actual fun of(value: Any): PlainMap? = when {
            value is Map<*, *> -> PlainMap(value, null)
            isPlainJsObject(value as JsAny) -> PlainMap(null, value)
            else -> null
        }
    }
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual class PlainList private constructor(private val list: List<*>?, private val js: JsArray<JsAny?>?) {
    actual constructor() : this(null, JsArray())

    actual val value: Any get() = list ?: js!!
    actual val size: Int get() = list?.size ?: js!!.length

    actual operator fun get(index: Int): Any? = if (list != null) list[index] else js!![index]

    actual operator fun set(index: Int, element: Any?) {
        js!![index] = element.toJs()
    }

    actual companion object {
        actual fun of(value: Any): PlainList? = when {
            value is List<*> -> PlainList(value, null)
            jsIsArray(value as JsAny) -> PlainList(null, value as JsArray<JsAny?>)
            else -> null
        }
    }
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual fun platformLeaf(value: Any): Any? {
    val js = value as JsAny
    return when (jsTypeOf(js)) {
        "string", "number", "boolean" -> js.toKotlin()
        else -> value
    }
}

private fun isPlainJsObject(value: JsAny): Boolean = js("Object.getPrototypeOf(value) === Object.prototype")

private fun jsHasOwn(target: JsAny, key: String): Boolean = js("Object.prototype.hasOwnProperty.call(target, key)")

/** An own data property, whatever the key: `target[key] = value` would run an inherited setter for `__proto__`. */
private fun jsDefine(target: JsAny, key: String, value: JsAny?) {
    js("Object.defineProperty(target, key, { value: value, enumerable: true, writable: true, configurable: true })")
}
