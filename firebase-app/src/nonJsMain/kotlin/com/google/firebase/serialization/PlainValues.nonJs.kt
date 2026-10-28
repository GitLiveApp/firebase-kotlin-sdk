/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

// The Android SDK, firebase-java-sdk and (through Kotlin/Native's interop with Foundation) the iOS SDK exchange Kotlin
// collections, so the plain values are the Kotlin ones, and their Cloud Functions serializers wrap a Long themselves.

internal actual fun toPlatformLeaf(value: Any?): Any? = value

internal actual val functionsSdkEncodesLongs: Boolean = true

internal actual class PlainMap private constructor(private val map: Map<*, *>) {
    actual constructor() : this(HashMap<String, Any?>())

    actual val value: Any get() = map
    actual val size: Int get() = map.size
    actual val keys: List<String> get() = map.keys.map { it.toString() }

    actual fun containsKey(key: String): Boolean = map.containsKey(key)

    actual operator fun get(key: String): Any? = map[key]

    @Suppress("UNCHECKED_CAST")
    actual operator fun set(key: String, element: Any?) {
        (map as MutableMap<String, Any?>)[key] = element
    }

    actual companion object {
        actual fun of(value: Any): PlainMap? = (value as? Map<*, *>)?.let(::PlainMap)
    }
}

internal actual class PlainList private constructor(private val list: List<*>) {
    actual constructor() : this(ArrayList<Any?>())

    actual val value: Any get() = list
    actual val size: Int get() = list.size

    actual operator fun get(index: Int): Any? = list[index]

    @Suppress("UNCHECKED_CAST")
    actual operator fun set(index: Int, element: Any?) {
        val list = list as MutableList<Any?>
        while (list.size <= index) list.add(null)
        list[index] = element
    }

    actual companion object {
        actual fun of(value: Any): PlainList? = (value as? List<*>)?.let(::PlainList)
    }
}
