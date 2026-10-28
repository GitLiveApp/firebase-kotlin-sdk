/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import platform.Foundation.NSNull

// Kotlin/Native maps the iOS SDK's NSDictionary, NSArray, NSString and NSNumber values to Kotlin collections, strings and
// numbers at the interop boundary, in both directions, except that a null inside a Foundation collection is an NSNull
// (a Kotlin null going the other way becomes one).
internal actual fun platformLeaf(value: Any): Any? = if (value is NSNull) null else value
