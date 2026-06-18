/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.externals

import kotlin.js.JsException
import kotlin.js.Promise
import kotlinx.coroutines.await

/*
 * Shared low-level JavaScript interop helpers for the `wasmJs` target.
 *
 * Unlike the `js` target, Kotlin/Wasm does not have the `dynamic` type and JS values do not
 * share a heap with Kotlin values. Every value crossing the JS boundary must be a [JsAny]
 * (or one of its `JsString`/`JsNumber`/`JsBoolean` leaves). These helpers replace the
 * `kotlin.js.Json`/`kotlin.js.json`/`dynamic` idioms used by the `js` source sets.
 */

// region Raw JS primitives

/** Creates an empty JS object literal `{}`. */
public fun jsObject(): JsAny = js("({})")

public fun jsGet(target: JsAny, key: String): JsAny? = js("target[key]")

public fun jsSet(target: JsAny, key: String, value: JsAny?) {
    js("target[key] = value")
}

/** `Object.keys(target)`. */
public fun jsObjectKeys(target: JsAny): JsArray<JsString> = js("Object.keys(target)")

/** `Object.entries(target)` as an array of `[key, value]` pairs. */
public fun jsObjectEntries(target: JsAny): JsArray<JsArray<JsAny?>> = js("Object.entries(target)")

/** Mirrors the `js` idiom `value != undefined` (intentionally a loose comparison, so an
 * explicit `null` is treated the same as a missing key). */
public fun jsContainsValue(target: JsAny, key: String): Boolean = js("target[key] != undefined")

public fun jsTypeOf(value: JsAny): String = js("typeof value")

/** `Array.isArray(value)`. */
public fun jsIsArray(value: JsAny): Boolean = js("Array.isArray(value)")

public fun jsonStringify(value: JsAny?): String = js("JSON.stringify(value)")

public fun jsonParse(value: String): JsAny? = js("JSON.parse(value)")

private fun jsAnyAsString(value: JsAny): String = js("value")
private fun jsAnyAsDouble(value: JsAny): Double = js("value")
private fun jsAnyAsBoolean(value: JsAny): Boolean = js("value")

/** Converts a [JsString] to a Kotlin [String]. */
public fun JsString.toKotlinString(): String = jsAnyAsString(this)

// endregion

// region JS error / Date helpers (replacing `dynamic` error access and `kotlin.js.Date`)

/** The `code` property of the JS `Error` wrapped by this [JsException], if present. */
public fun JsException.errorCode(): String? = thrownValue?.let { jsErrorCode(it) }

private fun jsErrorCode(error: JsAny): String? = js("error.code")

/** Stringifies the JS value thrown by this [JsException] (for diagnostics). */
public fun JsException.stringifyThrownValue(): String = jsonStringify(thrownValue)

/** Parses a date string the way JS `new Date(...)` would, returning epoch milliseconds. */
public fun parseDateStringToMillis(date: String): Double = js("new Date(date).getTime()")

// endregion

// region Promise await

/**
 * Awaits a [Promise] and returns its resolved value, typed by the promise's own parameter.
 *
 * `kotlinx.coroutines.await` on wasm has signature `Promise<JsAny?>.await(): T`, inferring its
 * result solely from call context — which fails whenever the awaited value is chained or assigned
 * without an explicit expected type. Keying the type parameter to the promise instead avoids that.
 */
@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
public suspend fun <T : JsAny?> Promise<T>.awaitValue(): T = (this as Promise<JsAny?>).await()

/** Awaits a [Promise] whose resolved value is discarded (mirrors `Promise<void>` in JS). */
@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
public suspend fun Promise<*>.awaitUnit() {
    (this as Promise<JsAny?>).awaitValue()
}

// endregion

// region Value conversion

/**
 * Converts a Kotlin value produced by the serialization framework (a primitive at the leaves)
 * into a JS value that can be stored inside a JS object/array. Values that are already [JsAny]
 * (nested structures, Firebase native types) are passed through untouched.
 */
public fun Any?.toJs(): JsAny? = when (this) {
    null -> null
    is Boolean -> toJsBoolean()
    is Byte -> toInt().toJsNumber()
    is Short -> toInt().toJsNumber()
    is Int -> toJsNumber()
    is Long -> toDouble().toJsNumber()
    is Float -> toDouble().toJsNumber()
    is Double -> toJsNumber()
    is Char -> toString().toJsString()
    is String -> toJsString()
    // `is JsAny` checks are not permitted on external interfaces in Kotlin/Wasm, so anything
    // that is not a known Kotlin leaf is assumed to already be a JS value (a nested structure
    // built by the encoder, or a Firebase native type passing through unchanged).
    else -> asJsAny()
}

/** Unchecked cast of a value already known to be a JS reference into [JsAny]. */
@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
public fun Any?.asJsAny(): JsAny = this as JsAny

/** Unchecked cast of a value already known to be a JS array into [JsArray]. */
@Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
public fun Any?.asJsArray(): JsArray<JsAny?> = this as JsArray<JsAny?>

/**
 * Converts a JS value read from a JS object/array into a Kotlin value understood by the shared
 * `decodeX(value: Any?)` helpers. JS primitives become their natural Kotlin equivalents
 * (string/number→Double/boolean); JS objects and arrays are kept as [JsAny] for further
 * structural decoding.
 */
public fun JsAny?.toKotlin(): Any? = when (this) {
    null -> null
    else -> when (jsTypeOf(this)) {
        "string" -> jsAnyAsString(this)
        "number" -> jsAnyAsDouble(this).toKotlinNumber()
        "boolean" -> jsAnyAsBoolean(this)
        else -> this
    }
}

/**
 * JS has a single number type, so — matching the `js` target's behaviour where whole JS numbers
 * satisfy `is Int` — a whole number is surfaced as the narrowest of [Int]/[Long] and anything
 * fractional (or beyond safe-integer range) stays a [Double]. All `decodeX` helpers accept any
 * [Number], so this only affects equality of raw, untyped decoded values.
 */
private fun Double.toKotlinNumber(): Any = when {
    isNaN() || isInfinite() -> this
    this != toLong().toDouble() -> this
    this < Int.MIN_VALUE.toDouble() || this > Int.MAX_VALUE.toDouble() -> toLong()
    else -> toInt()
}

// endregion

// region Builders

/** Builds a JS object from the given key/value pairs, converting Kotlin leaves via [toJs]. */
public fun json(vararg pairs: Pair<String, Any?>): JsAny {
    val target = jsObject()
    for ((key, value) in pairs) {
        jsSet(target, key, value.toJs())
    }
    return target
}

/** Builds a JS array from the given Kotlin elements, converting leaves via [toJs]. */
public fun jsArrayOf(elements: List<Any?>): JsArray<JsAny?> {
    val array = JsArray<JsAny?>()
    elements.forEachIndexed { index, element ->
        array[index] = element.toJs()
    }
    return array
}

/** Reads a JS array into a Kotlin list, converting leaves via [toKotlin]. */
public fun JsArray<JsAny?>.toKotlinList(): List<Any?> = buildList {
    for (index in 0 until this@toKotlinList.length) {
        add(this@toKotlinList[index].toKotlin())
    }
}

/** Iterates a JS array of references into a Kotlin [List] without value conversion. */
public fun <T : JsAny?> JsArray<T>.toList(): List<T> {
    val list = ArrayList<T>(length)
    for (index in 0 until length) {
        @Suppress("UNCHECKED_CAST")
        list.add(this[index] as T)
    }
    return list
}

// endregion
