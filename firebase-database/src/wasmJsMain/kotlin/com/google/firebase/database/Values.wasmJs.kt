/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.jsIsArray
import dev.gitlive.firebase.externals.jsObjectKeys
import dev.gitlive.firebase.externals.jsTypeOf
import dev.gitlive.firebase.externals.toKotlin
import dev.gitlive.firebase.externals.toKotlinString
import dev.gitlive.firebase.externals.toList
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
@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal fun Any?.toJs(): JsAny? = databaseFormat.convertToPlainValue(this) as JsAny?

/** A plain JS value as its natural Kotlin type. */
@Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal fun JsAny?.fromJs(): Any? {
    if (this == null) return null
    return when (jsTypeOf(this)) {
        "undefined" -> null
        "string", "boolean" -> toKotlin()
        "number" -> {
            val number = (this as JsNumber).toDouble()
            if (number == floor(number) && abs(number) < MAX_EXACT_INTEGER) number.toLong() else number
        }
        "object" -> if (jsIsArray(this)) {
            (this as JsArray<JsAny?>).toList().map { it.fromJs() }
        } else {
            objectKeys(this).associateWith { key -> jsGet(this, key).fromJs() }
        }
        else -> this
    }
}

/** `Object.keys` of a JS object. */
internal fun objectKeys(value: JsAny): List<String> = jsObjectKeys(value).toList().map { it.toKotlinString() }

/** `String(value)`, the JS SDK's description of a query or reference. */
internal fun jsToString(value: JsAny): String = js("String(value)")
