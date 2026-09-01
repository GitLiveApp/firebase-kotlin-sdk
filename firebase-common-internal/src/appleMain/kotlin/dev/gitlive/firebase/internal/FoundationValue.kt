/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.internal

import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding

/**
 * Rebuilds an encoded container as Foundation objects so that booleans reach the Firebase
 * SDKs as CFBoolean instances.
 *
 * The Firebase Apple SDKs only serialize an NSNumber as a JSON boolean if it is one of the
 * CFBoolean singletons. A Kotlin [Boolean] crossing the interop boundary is boxed as a
 * different NSNumber subclass, so it is stored as the number 1/0 instead
 * (https://github.com/GitLiveApp/firebase-kotlin-sdk/issues/275). Kotlin/Native also bridges
 * any CFBoolean entering Kotlin back to a Kotlin [Boolean], so the CFBoolean can never be
 * held in Kotlin code directly. The only way to hand one to the SDK is inside a Foundation
 * container that Kotlin never unwraps: this function serializes the container to JSON text,
 * where boolean identity is still known, and parses it with NSJSONSerialization. The parsed
 * result is a lazy Foundation-backed view, so the CFBooleans inside survive untouched.
 *
 * Containers without booleans are returned unchanged. Scalars are returned unchanged as well,
 * a lone Boolean cannot be fixed this way because it would bridge back to a Kotlin Boolean,
 * so callers must wrap boolean roots in a container themselves (for example the Realtime
 * Database's {".value": x} leaf form). If the container holds anything that JSON cannot
 * represent (custom objects, non-String map keys, non-finite doubles, chars), it is returned
 * unchanged to preserve the previous behavior for those values.
 */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
public fun Any?.withFoundationBooleans(): Any? {
    if (this !is Map<*, *> && this !is List<*>) return this
    if (!containsBoolean(this)) return this
    val json = try {
        buildString { appendJsonValue(this@withFoundationBooleans) }
    } catch (e: NotJsonRepresentable) {
        return this
    }

    @Suppress("CAST_NEVER_SUCCEEDS")
    val data = (json as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return this
    return NSJSONSerialization.JSONObjectWithData(data, 0u, null) ?: this
}

private class NotJsonRepresentable : Exception()

private fun containsBoolean(value: Any?): Boolean = when (value) {
    is Boolean -> true
    is Map<*, *> -> value.values.any(::containsBoolean)
    is List<*> -> value.any(::containsBoolean)
    else -> false
}

private fun StringBuilder.appendJsonValue(value: Any?) {
    when (value) {
        null -> append("null")
        is Boolean -> append(if (value) "true" else "false")
        is Double -> if (value.isFinite()) append(value.toString()) else throw NotJsonRepresentable()
        // Widened to Double first: a directly bridged Float NSNumber reaches the SDK as the
        // Double widening of the Float bits (1.2f becomes 1.2000000476837158), and the text
        // form must produce that same Double so boolean presence never changes stored numbers.
        is Float -> appendJsonValue(value.toDouble())
        is Byte, is Short, is Int, is Long -> append(value.toString())
        is String -> appendJsonString(value)
        is Map<*, *> -> {
            append('{')
            value.entries.forEachIndexed { index, (key, entryValue) ->
                if (index > 0) append(',')
                appendJsonString(key as? String ?: throw NotJsonRepresentable())
                append(':')
                appendJsonValue(entryValue)
            }
            append('}')
        }
        is List<*> -> {
            append('[')
            value.forEachIndexed { index, element ->
                if (index > 0) append(',')
                appendJsonValue(element)
            }
            append(']')
        }
        else -> throw NotJsonRepresentable()
    }
}

private fun StringBuilder.appendJsonString(value: String) {
    append('"')
    for (char in value) {
        when {
            char == '"' -> append("\\\"")
            char == '\\' -> append("\\\\")
            char == '\n' -> append("\\n")
            char == '\r' -> append("\\r")
            char == '\t' -> append("\\t")
            char < ' ' -> append("\\u").append(char.code.toString(16).padStart(4, '0'))
            else -> append(char)
        }
    }
    append('"')
}
