/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect

import org.junit.Ignore

actual val emulatorHost: String = "10.0.2.2"
actual val context: Any = ""
actual val deletesAppsBetweenTests: Boolean = true

// org.junit.Ignore itself: kotlin.test.Ignore is a type alias on the JVM, which an actual type alias may not point to.
actual typealias IgnoreForAndroidUnitTest = Ignore
