/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import dev.gitlive.firebase.externals.jsIsArray
import dev.gitlive.firebase.externals.jsObjectEntries
import dev.gitlive.firebase.externals.jsTypeOf
import dev.gitlive.firebase.externals.toJs
import dev.gitlive.firebase.externals.toKotlin

/*
 * The JS SDK returns the values JSON.parse produces, held as JsAny. A Kotlin value cast to JsAny is an opaque reference
 * on the JS side (typeof "object", no prototype), so the checks below are false for it and it is returned as it is.
 */

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual fun platformValueToPlain(value: Any): Any? {
    val js = value as JsAny
    return when {
        jsIsArray(js) -> (js as JsArray<JsAny?>).let { array -> List(array.length) { array[it].toKotlin() } }
        isPlainJsObject(js) -> jsObjectEntries(js).let { entries ->
            LinkedHashMap<String, Any?>(entries.length).also { map ->
                for (index in 0 until entries.length) {
                    val entry = entries[index]!!
                    map[entry[0].toKotlin() as String] = entry[1].toKotlin()
                }
            }
        }
        else -> when (jsTypeOf(js)) {
            "string", "number", "boolean" -> js.toKotlin()
            else -> value
        }
    }
}

internal actual fun plainValueToPlatform(value: Any?): Any? = value.toJs()

private fun isPlainJsObject(value: JsAny): Boolean = js("Object.getPrototypeOf(value) === Object.prototype")
