/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

/**
 * A value a [FirebaseFormat] has already converted (the result of [FirebaseFormat.convertToPlainValue] or
 * [FirebaseFormat.encodeToPlainValue]), which the formats pass through as it is: the `com.google.firebase` layers convert
 * what they are given in one place and hand the result on to platform code that converts whatever it receives, so this
 * marks the values that need no second pass.
 */
public class PlainValue(public val value: Any?)
