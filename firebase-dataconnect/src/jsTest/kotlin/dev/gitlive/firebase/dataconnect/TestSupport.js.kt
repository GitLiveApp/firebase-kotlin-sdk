/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect

actual val emulatorHost: String = "localhost"
actual val context: Any = Unit
actual val deletesAppsBetweenTests: Boolean = true

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
actual annotation class IgnoreForAndroidUnitTest
