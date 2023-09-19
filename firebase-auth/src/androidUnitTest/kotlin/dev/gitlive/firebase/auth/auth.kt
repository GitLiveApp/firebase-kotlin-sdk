/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")
package dev.gitlive.firebase.auth

import org.junit.Ignore

actual val emulatorHost: String = "10.0.2.2"

actual val context: Any = ""

actual fun runTest(test: suspend () -> Unit) = kotlinx.coroutines.test.runTest { test() }
actual typealias IgnoreForAndroidUnitTest = Ignore