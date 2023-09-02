/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest

actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = runTest { test() }
