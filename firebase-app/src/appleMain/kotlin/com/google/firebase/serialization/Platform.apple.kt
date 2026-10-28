/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlinx.cinterop.toKString
import platform.Foundation.NSNull
import platform.Foundation.NSNumber

// Kotlin/Native maps the iOS SDK's NSDictionary, NSArray, NSString and NSNumber values to Kotlin collections, strings,
// numbers and booleans at the interop boundary, in both directions, with two exceptions the format bridges:
// - a null inside a Foundation collection is an NSNull (a Kotlin null going the other way becomes one);
// - a Kotlin Boolean going out is boxed as Kotlin/Native's own NSNumber subclass, which Foundation does not take for a
//   boolean (NSJSONSerialization writes it as the number 1), so the format writes Foundation's boolean NSNumber instead
//   and reads one back as a Boolean.

internal actual fun platformLeaf(value: Any): Any? = when (value) {
    is NSNull -> null
    is NSNumber -> if (value.isBoolean) value.boolValue else value
    else -> value
}

internal actual fun toPlatformLeaf(value: Any?): Any? = if (value is Boolean) NSNumber.numberWithBool(value) else value

// A Foundation boolean's objCType is "c" (a char) on arm64 and "B" (a BOOL) elsewhere; the Firestore iOS SDK reads both as
// a boolean too, since no other value is encoded as a char in practice.
private val NSNumber.isBoolean: Boolean
    get() = objCType.toKString().let { it == "c" || it == "B" }
