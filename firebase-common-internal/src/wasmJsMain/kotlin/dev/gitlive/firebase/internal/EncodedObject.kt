/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.internal

import dev.gitlive.firebase.externals.asJsAny
import dev.gitlive.firebase.externals.jsGet
import dev.gitlive.firebase.externals.jsIsArray
import dev.gitlive.firebase.externals.jsObjectKeys
import dev.gitlive.firebase.externals.json
import dev.gitlive.firebase.externals.toKotlin

public val EncodedObject.js: JsAny get() = json(*getRaw().entries.map { (key, value) -> key to value }.toTypedArray())

@PublishedApi
internal actual fun Any.asNativeMap(): Map<*, *>? {
    val obj: JsAny = when (this) {
        is Number -> return null
        is Boolean -> return null
        is String -> return null
        is Map<*, *> -> {
            if (keys.all { it is String }) {
                json(*entries.map { (key, value) -> (key as String) to value }.toTypedArray())
            } else {
                return null
            }
        }
        is Collection<*> -> return null
        is Array<*> -> return null
        else -> asJsAny().takeUnless { jsIsArray(it) } ?: return null
    }
    val mutableMap = mutableMapOf<String, Any?>()
    val keys = jsObjectKeys(obj)
    for (index in 0 until keys.length) {
        val key = keys[index].toKotlin() as String
        mutableMap[key] = jsGet(obj, key).toKotlin()
    }
    return mutableMap.toMap()
}
