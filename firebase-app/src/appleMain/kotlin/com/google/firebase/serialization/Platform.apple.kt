/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlinx.cinterop.reinterpret
import platform.CoreFoundation.CFArrayAppendValue
import platform.CoreFoundation.CFArraySetValueAtIndex
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSArray
import platform.Foundation.NSMutableArray
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSNull
import platform.Foundation.arrayWithObject

// Kotlin/Native maps a Foundation collection the iOS SDK hands over to a Kotlin Map or List viewing it, and its NSString
// and NSNumber elements to Kotlin strings, numbers and booleans as they are read, so the plain values are those Kotlin
// ones. A null inside a Foundation collection is an NSNull, which the format reads as null and writes for a null.
//
// The format writes into Foundation collections of its own, because a Kotlin collection handed to the SDK is bridged
// with its Kotlin booleans boxed as Kotlin/Native's own NSNumber subclass, which Foundation does not take for a boolean
// (NSJSONSerialization writes it as the number 1). It writes through the CoreFoundation API, where a boolean is
// Foundation's own kCFBooleanTrue/False, which Kotlin/Native would turn back into a Kotlin Boolean on any Kotlin-typed
// reference to it; the Kotlin view of a collection keeps the Foundation object behind it, so a map the encoder re-opens
// through [PlainMap.of] is written to the same way.

internal actual fun platformLeaf(value: Any): Any? = if (value is NSNull) null else value

internal actual fun toPlatformLeaf(value: Any?): Any? = value

internal actual class PlainMap private constructor(private val map: Map<*, *>) {
    actual constructor() : this(kotlinView(NSMutableDictionary()) as Map<*, *>)

    actual val value: Any get() = map
    actual val size: Int get() = map.size
    actual val keys: List<String> get() = map.keys.map { it.toString() }

    actual fun containsKey(key: String): Boolean = map.containsKey(key)

    actual operator fun get(key: String): Any? = map[key]

    actual operator fun set(key: String, element: Any?) {
        withCFRef(map) { dictionary ->
            withCFRef(key) { cfKey -> withCFValue(element) { CFDictionarySetValue(dictionary.reinterpret(), cfKey, it) } }
        }
    }

    actual companion object {
        actual fun of(value: Any): PlainMap? = (value as? Map<*, *>)?.let(::PlainMap)
    }
}

internal actual class PlainList private constructor(private val list: List<*>) {
    actual constructor() : this(kotlinView(NSMutableArray()) as List<*>)

    actual val value: Any get() = list
    actual val size: Int get() = list.size

    actual operator fun get(index: Int): Any? = list[index]

    actual operator fun set(index: Int, element: Any?) {
        withCFRef(list) { array ->
            while (list.size <= index) withCFRef(NSNull.`null`()) { CFArrayAppendValue(array.reinterpret(), it) }
            withCFValue(element) { CFArraySetValueAtIndex(array.reinterpret(), index.toLong(), it) }
        }
    }

    actual companion object {
        actual fun of(value: Any): PlainList? = (value as? List<*>)?.let(::PlainList)
    }
}

/** The Kotlin collection Kotlin/Native gives for a Foundation [collection] it meets as an `id`: a view of it. */
private fun kotlinView(collection: Any): Any? = NSArray.arrayWithObject(collection)[0]

/** Runs [block] with the Foundation object behind [value] as a retained CoreFoundation reference. */
private inline fun <T> withCFRef(value: Any, block: (CFTypeRef) -> T): T {
    val ref = checkNotNull(CFBridgingRetain(value))
    try {
        return block(ref)
    } finally {
        CFRelease(ref)
    }
}

/** Runs [block] with the CoreFoundation reference to store for an element: a Foundation boolean, an NSNull or the object. */
private inline fun <T> withCFValue(element: Any?, block: (CFTypeRef?) -> T): T = when (element) {
    is Boolean -> block(if (element) kCFBooleanTrue else kCFBooleanFalse)
    else -> withCFRef(element ?: NSNull.`null`(), block)
}
