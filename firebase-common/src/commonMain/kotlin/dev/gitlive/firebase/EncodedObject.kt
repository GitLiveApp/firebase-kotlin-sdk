/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

/**
 * Platform specific object for storing encoded data that can be used for methods that explicitly require an object.
 * This is essentially a [Map] of [String] and [Any]? (as represented by [raw]) but since [encode] gives a platform specific value, this method wraps that.
 */
expect interface EncodedObject {
    val raw: Map<String, Any?>
}
