@file:JvmName("TestUtilsJvm")
/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import android.app.Application
import com.google.firebase.FirebasePlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.minutes

val testContext = Application().apply {
    FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
        val storage = mutableMapOf<String, String>()
        override fun store(key: String, value: String) = storage.set(key, value)
        override fun retrieve(key: String) = storage[key]
        override fun clear(key: String) {
            storage.remove(key)
        }
        override fun log(msg: String) = println(msg)
    })
}

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = kotlinx.coroutines.test.runTest(timeout = 5.minutes) { test() }
actual fun runBlockingTest(action: suspend CoroutineScope.() -> Unit) = runBlocking(block = action)

actual fun nativeMapOf(vararg pairs: Pair<Any, Any?>): Any = mapOf(*pairs)
actual fun nativeListOf(vararg elements: Any?): Any = listOf(*elements)
actual fun nativeAssertEquals(expected: Any?, actual: Any?) {
    kotlin.test.assertEquals(expected, actual)
}
