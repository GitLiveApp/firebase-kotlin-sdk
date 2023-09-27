@file:JvmName("TestUtilsJVM")
/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestResult
import kotlin.jvm.JvmName

expect fun runTest(test: suspend CoroutineScope.() -> Unit): TestResult
expect fun runBlockingTest(action: suspend CoroutineScope.() -> Unit)

expect fun nativeMapOf(vararg pairs: Pair<String, Any?>): Any
expect fun nativeListOf(vararg elements: Any): Any
expect fun nativeAssertEquals(expected: Any?, actual: Any?)

val firebaseOptions = FirebaseOptions(
    applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
    apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
    databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
    storageBucket = "fir-kotlin-sdk.appspot.com",
    projectId = "fir-kotlin-sdk",
    gcmSenderId = "846484016111"
)
