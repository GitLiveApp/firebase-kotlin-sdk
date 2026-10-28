/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlinx.cinterop.CFBridgingRetain
import kotlinx.cinterop.reinterpret
import platform.CoreFoundation.CFArraySetValueAtIndex
import platform.CoreFoundation.CFBooleanRef
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFBooleanFalse
import platform.CoreFoundation.kCFBooleanTrue
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
// (NSJSONSerialization writes it as the number 1); a boolean is set through the CoreFoundation API as Foundation's own
// kCFBooleanTrue/False, which Kotlin/Native would turn back into a Kotlin Boolean on any Kotlin-typed reference to it.

internal actual fun platformLeaf(value: Any): Any? = if (value is NSNull) null else value

internal actual fun toPlatformLeaf(value: Any?): Any? = value

internal actual class PlainMap private constructor(private val map: Map<*, *>, private val dictionary: NSMutableDictionary?) {
    actual constructor() : this(NSMutableDictionary())

    private constructor(dictionary: NSMutableDictionary) : this(kotlinView(dictionary) as Map<*, *>, dictionary)

    actual val value: Any get() = map
    actual val size: Int get() = map.size
    actual val keys: List<String> get() = map.keys.map { it.toString() }

    actual fun containsKey(key: String): Boolean = map.containsKey(key)

    actual operator fun get(key: String): Any? = map[key]

    actual operator fun set(key: String, element: Any?) {
        val dictionary = checkNotNull(dictionary) { "A plain value read from the SDK is not written to" }
        when (element) {
            null -> dictionary.setObject(NSNull.`null`(), key)
            is Boolean -> withCFRef(dictionary) { target ->
                withCFRef(key) { CFDictionarySetValue(target.reinterpret(), it, cfBoolean(element)) }
            }
            else -> dictionary.setObject(element, key)
        }
    }

    actual companion object {
        actual fun of(value: Any): PlainMap? = (value as? Map<*, *>)?.let { PlainMap(it, null) }
    }
}

internal actual class PlainList private constructor(private val list: List<*>, private val array: NSMutableArray?) {
    actual constructor() : this(NSMutableArray())

    private constructor(array: NSMutableArray) : this(kotlinView(array) as List<*>, array)

    actual val value: Any get() = list
    actual val size: Int get() = list.size

    actual operator fun get(index: Int): Any? = list[index]

    actual operator fun set(index: Int, element: Any?) {
        val array = checkNotNull(array) { "A plain value read from the SDK is not written to" }
        while (array.count.toInt() <= index) array.addObject(NSNull.`null`())
        when (element) {
            null -> array.replaceObjectAtIndex(index.toULong(), NSNull.`null`())
            is Boolean -> withCFRef(array) { CFArraySetValueAtIndex(it.reinterpret(), index.toLong(), cfBoolean(element)) }
            else -> array.replaceObjectAtIndex(index.toULong(), element)
        }
    }

    actual companion object {
        actual fun of(value: Any): PlainList? = (value as? List<*>)?.let { PlainList(it, null) }
    }
}

/** The Kotlin collection Kotlin/Native gives for a Foundation [collection] it meets as an `id`: a view of it. */
private fun kotlinView(collection: Any): Any = NSArray.arrayWithObject(collection).objectAtIndex(0u)

private fun cfBoolean(value: Boolean): CFBooleanRef? = if (value) kCFBooleanTrue else kCFBooleanFalse

/** Runs [block] with the Foundation object behind [value] as a retained CoreFoundation reference. */
private inline fun <T> withCFRef(value: Any, block: (CFTypeRef) -> T): T {
    val ref = checkNotNull(CFBridgingRetain(value))
    try {
        return block(ref)
    } finally {
        CFRelease(ref)
    }
}
