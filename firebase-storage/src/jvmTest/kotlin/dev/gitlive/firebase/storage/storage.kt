/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")

package dev.gitlive.firebase.storage

import dev.gitlive.firebase.testContext

actual val emulatorHost: String = "localhost"

// Accessed before the first request of every test class, so the emulator header fix is in place for all of them.
actual val context: Any = testContext.also { CaseInsensitiveResponseHeaders.install() }

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest
