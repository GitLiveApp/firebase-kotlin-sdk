/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")
package dev.gitlive.firebase

import org.junit.Ignore

actual val context: Any = ""

actual fun runTest(test: suspend () -> Unit) = kotlinx.coroutines.test.runTest { test() }
actual typealias IgnoreForAndroidUnitTest = Ignore
