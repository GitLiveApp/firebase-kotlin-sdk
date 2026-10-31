/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")

package dev.gitlive.firebase.functions

import dev.gitlive.firebase.testContext

actual val emulatorHost: String = "localhost"
actual val context: Any = testContext

actual annotation class IgnoreForAndroidUnitTest
