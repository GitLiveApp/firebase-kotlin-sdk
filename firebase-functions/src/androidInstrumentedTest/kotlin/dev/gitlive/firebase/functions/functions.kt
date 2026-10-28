/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")

package dev.gitlive.firebase.functions

import androidx.test.platform.app.InstrumentationRegistry

actual val emulatorHost: String = "10.0.2.2"
actual val context: Any = InstrumentationRegistry.getInstrumentation().targetContext

actual annotation class IgnoreForAndroidUnitTest
