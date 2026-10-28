/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlin.reflect.KClass

/** The class's name as `Class.getName()` returns it on Android, for messages; elsewhere the closest name available. */
internal expect fun javaClassNameOf(type: KClass<*>): String

/** Logs a warning as the Android SDK's mappers do, with `android.util.Log.w` on Android. */
internal expect fun logWarning(tag: String, message: String)

/**
 * Converts [value] when it is a value of this platform's Firebase SDK rather than a plain value, and returns it as it is
 * otherwise. The Android, JVM and Apple SDKs return plain values, so there it is the identity. The JS SDK returns JS
 * values: a JS primitive becomes its Kotlin counterpart and a JS object or array becomes a [Map] or [List] whose elements
 * are still JS values, so a structure is converted one level at a time as the format reads it, in a single pass.
 */
internal expect fun platformValueToPlain(value: Any): Any?

/**
 * Converts plain values to what this platform's Firebase SDK accepts: the identity on Android, the JVM and Apple, whose
 * SDKs take plain values; on JS and wasmJs a [Map] becomes a JS object, a [List] a JS array and a [Long] a JS number,
 * recursively, and any other value is passed through.
 */
internal expect fun plainValueToPlatform(value: Any?): Any?

/** [platformValueToPlain] for any value; Kotlin primitives, the usual leaves, skip the platform check. */
internal fun Any?.toPlainValue(): Any? = when (this) {
    null, is String, is Number, is Boolean -> this
    else -> platformValueToPlain(this)
}

/** [toPlainValue] applied to a whole tree, for a value read into `Any`; a tree of plain values is returned as it is. */
internal fun platformValueToPlainDeep(value: Any?): Any? = when (val plain = value.toPlainValue()) {
    is Map<*, *> -> {
        var changed = plain !== value
        val result = LinkedHashMap<Any?, Any?>(plain.size)
        for ((key, element) in plain) result[key] = platformValueToPlainDeep(element).also { if (it !== element) changed = true }
        if (changed) result else plain
    }
    is List<*> -> {
        var changed = plain !== value
        val result = plain.map { element -> platformValueToPlainDeep(element).also { if (it !== element) changed = true } }
        if (changed) result else plain
    }
    else -> plain
}
