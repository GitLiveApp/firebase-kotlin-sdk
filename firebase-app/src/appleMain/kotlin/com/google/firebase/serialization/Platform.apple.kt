/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

// Kotlin/Native maps the iOS SDK's NSDictionary, NSArray, NSString and NSNumber values to Kotlin collections, strings and
// numbers at the interop boundary, in both directions, so the iOS SDK returns and accepts plain values.
internal actual fun platformValueToPlain(value: Any): Any? = value

internal actual fun plainValueToPlatform(value: Any?): Any? = value
