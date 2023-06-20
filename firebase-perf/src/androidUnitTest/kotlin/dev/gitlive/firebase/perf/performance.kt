/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")
package dev.gitlive.firebase.perf

import kotlinx.coroutines.CoroutineScope
import org.junit.Ignore

actual val emulatorHost: String = "10.0.2.2"

actual val context: Any = ""

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = kotlinx.coroutines.test.runTest { test() }

// Tests are to be run on AndroidInstrumentedTests.
// Kotlin 1.8 does not allow us to remove the commonTest dependency from AndroidUnitTest
// Therefore we just wont run them
// Kotlin 1.9 will introduce methods for disabling tests properly
actual typealias IgnoreForAndroidUnitTest = Ignore
