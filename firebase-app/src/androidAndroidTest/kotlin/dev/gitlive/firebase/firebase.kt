/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("test")
package dev.gitlive.firebase

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking

actual val context: Any = InstrumentationRegistry.getInstrumentation().targetContext

actual fun runTest(test: suspend () -> Unit) = runBlocking { test() }

object Firebase

expect class FirebaseApp {
    val name: String
    val options: FirebaseOptions