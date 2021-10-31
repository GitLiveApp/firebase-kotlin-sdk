/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")
package dev.gitlive.firebase

import kotlinx.coroutines.runBlocking

actual val context: Any = Unit

actual val firebaseOptions: FirebaseOptions
    get() = AdminFirebaseOptions("kotlin-sdk-test-firebase-adminsdk-22xsa-d938f2f196.json")

actual fun runTest(test: suspend () -> Unit) = runBlocking { test() }
