/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import kotlin.time.Duration.Companion.minutes


actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

actual fun runTest(test: suspend () -> Unit) = kotlinx.coroutines.test.runTest(timeout = 5.minutes) { test() }

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest
