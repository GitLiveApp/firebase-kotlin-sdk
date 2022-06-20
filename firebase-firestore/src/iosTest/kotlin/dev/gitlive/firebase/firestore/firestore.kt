/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import platform.Foundation.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = runBlocking {
    val testRun = MainScope().async { test() }
    while (testRun.isActive) {
        NSRunLoop.mainRunLoop.runMode(
            NSDefaultRunLoopMode,
            beforeDate = NSDate.create(timeInterval = 1.0, sinceDate = NSDate())
        )
        yield()
    }
    testRun.await()
}

actual fun encodedAsMap(encoded: Any?): Map<String, Any?> = encoded as Map<String, Any?>
actual fun Map<String, Any?>.asEncoded(): Any = this
