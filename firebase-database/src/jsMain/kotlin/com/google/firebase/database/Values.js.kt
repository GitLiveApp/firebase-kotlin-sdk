/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import kotlin.math.abs
import kotlin.math.floor

private const val MAX_EXACT_INTEGER = 9007199254740992.0

/*
 * The JS SDK reads and writes plain JS values; the layer presents them as the natural Kotlin types the Android SDK
 * uses (Map, List, String, Boolean, Long, Double), integral numbers reading as Long like the Android SDK's.
 */

/**
 * [this] as the plain JS value the JS SDK takes, through the format: Kotlin maps become objects, collections arrays and
 * longs numbers, and a `@Serializable` class is mapped with the Android SDK's rules (see [databaseFormat]).
 */
internal fun Any?.toJs(): Any? = databaseFormat.convertToPlainValue(this)

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
