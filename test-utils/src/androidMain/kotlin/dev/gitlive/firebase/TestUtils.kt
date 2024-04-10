@file:JvmName("TestUtilsAndroid")
/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.minutes

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = kotlinx.coroutines.test.runTest(timeout = 5.minutes) { test() }
actual fun runBlockingTest(action: suspend CoroutineScope.() -> Unit) = runBlocking(block = action)

actual fun nativeMapOf(vararg pairs: Pair<Any, Any?>): Any = mapOf(*pairs)
actual fun nativeListOf(vararg elements: Any?): Any = listOf(*elements)
actual fun nativeAssertEquals(expected: Any?, actual: Any?) {
    kotlin.test.assertEquals(expected, actual)
}
