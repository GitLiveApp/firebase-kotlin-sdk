/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import kotlin.js.json
import kotlin.math.abs
import kotlin.math.floor

private const val MAX_EXACT_INTEGER = 9007199254740992.0

/*
 * The JS SDK reads and writes plain JS values; the layer presents them as the natural Kotlin types the Android SDK
 * uses (Map, List, String, Boolean, Long, Double), integral numbers reading as Long like the Android SDK's.
 */

/** [this] as a plain JS value: Kotlin maps become objects, collections arrays, longs numbers. */
internal fun Any?.toJs(): Any? = when (this) {
    null -> null
    is String, is Boolean -> this
    is Long -> toDouble()
    is Number -> this
    is Map<*, *> -> json(*entries.map { (key, value) -> key.toString() to value.toJs() }.toTypedArray())
    is Collection<*> -> map { it.toJs() }.toTypedArray()
    is Array<*> -> map { it.toJs() }.toTypedArray()
    else -> this
}

/** A plain JS value as its natural Kotlin type. */
internal fun Any?.fromJs(): Any? {
    if (this == null || this == undefined) return null
    return when (jsTypeOf(this)) {
        "string", "boolean" -> this
        "number" -> {
            val number = unsafeCast<Double>()
            if (number == floor(number) && abs(number) < MAX_EXACT_INTEGER) number.toLong() else number
        }
        "object" -> if (isArray(this)) {
            unsafeCast<Array<Any?>>().map { it.fromJs() }
        } else {
            objectKeys(this).associateWith { key -> asDynamic()[key].unsafeCast<Any?>().fromJs() }
        }
        else -> this
    }
}

internal fun isArray(value: Any?): Boolean = js("Array.isArray")(value).unsafeCast<Boolean>()

internal fun objectKeys(value: Any?): Array<String> = js("Object.keys")(value).unsafeCast<Array<String>>()
