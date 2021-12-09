/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")
package dev.gitlive.firebase

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking

actual val context: Any = InstrumentationRegistry.getInstrumentation().targetContext

actual val firebaseOptions: CommonFirebaseOptions
    get() =
        FirebaseOptions(
            applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
            apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
            databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
            storageBucket = "fir-kotlin-sdk.appspot.com",
            projectId = "fir-kotlin-sdk",
            gcmSenderId = "846484016111"
        )

actual fun runTest(test: suspend () -> Unit) = runBlocking { test() }
