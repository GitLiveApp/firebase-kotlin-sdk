/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlin.js.json

actual fun nativeMapOf(vararg pairs: Pair<String, Any>): Any = json(*pairs)
actual fun nativeListOf(vararg elements: Any): Any = elements
actual fun nativeAssertEquals(expected: Any?, actual: Any?)  = kotlin.test.assertEquals(JSON.stringify(expected), JSON.stringify(actual))