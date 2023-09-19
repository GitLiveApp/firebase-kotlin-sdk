/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

expect fun nativeMapOf(vararg pairs: Pair<Any, Any?>): Any
expect fun nativeListOf(vararg elements: Any): Any
expect fun nativeAssertEquals(expected: Any?, actual: Any?)
