/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")
package dev.gitlive.firebase.database

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.TestResult

actual val emulatorHost: String = "10.0.2.2"

actual val context: Any = InstrumentationRegistry.getInstrumentation().targetContext
actual fun runTest(test: suspend () -> Unit) = kotlinx.coroutines.test.runTest { test() }
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest
