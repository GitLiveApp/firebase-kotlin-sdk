/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect

import dev.gitlive.firebase.testContext

actual val emulatorHost: String = "localhost"
actual val context: Any = testContext

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest
