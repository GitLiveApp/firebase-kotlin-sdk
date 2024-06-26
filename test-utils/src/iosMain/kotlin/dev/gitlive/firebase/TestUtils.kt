/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import platform.Foundation.NSDate
import platform.Foundation.NSDefaultRunLoopMode
import platform.Foundation.NSRunLoop
import platform.Foundation.create
import platform.Foundation.runMode

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = runBlocking {
    val testRun = MainScope().async { test() }
    while (testRun.isActive) {
        NSRunLoop.mainRunLoop.runMode(
            NSDefaultRunLoopMode,
            beforeDate = NSDate.create(timeInterval = 1.0, sinceDate = NSDate()),
        )
        yield()
    }
    testRun.await()
}
actual fun runBlockingTest(action: suspend CoroutineScope.() -> Unit) = runBlocking(block = action)
actual fun nativeMapOf(vararg pairs: Pair<Any, Any?>): Any = mapOf(*pairs)
actual fun nativeListOf(vararg elements: Any?): Any = listOf(*elements)
actual fun nativeAssertEquals(expected: Any?, actual: Any?) {
    kotlin.test.assertEquals(expected, actual)
}
