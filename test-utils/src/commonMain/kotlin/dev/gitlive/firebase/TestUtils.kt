/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestResult
expect fun runTest(test: suspend CoroutineScope.() -> Unit): TestResult
expect fun runBlockingTest(action: suspend CoroutineScope.() -> Unit)

expect fun nativeMapOf(vararg pairs: Pair<Any, Any?>): Any
expect fun nativeListOf(vararg elements: Any): Any
expect fun nativeAssertEquals(expected: Any?, actual: Any?)
