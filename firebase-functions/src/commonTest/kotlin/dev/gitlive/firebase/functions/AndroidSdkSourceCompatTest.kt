/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.HttpsCallableOptions
import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult
import com.google.firebase.functions.functions
import com.google.firebase.functions.getHttpsCallable
import com.google.firebase.functions.getHttpsCallableFromUrl
import com.google.firebase.functions.setTimeout
import com.google.firebase.functions.withTimeout
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runTest
import kotlinx.coroutines.tasks.await
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Exercises the `com.google.firebase.functions` layer exactly as Android app code would (static accessors, Task API,
 * options builder, the Duration forms of the timeout members and the String forms of the URL members), on every
 * platform, against the Functions emulator.
 */
@IgnoreForAndroidUnitTest
class AndroidSdkSourceCompatTest {

    private lateinit var functions: FirebaseFunctions

    @BeforeTest
    fun initializeFirebase() {
        val app = dev.gitlive.firebase.Firebase.apps(context).firstOrNull() ?: dev.gitlive.firebase.Firebase.initialize(
            context,
            dev.gitlive.firebase.FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
        )
        functions = FirebaseFunctions.getInstance(app.compat).apply { useEmulator(emulatorHost, 5001) }
    }

    @Test
    fun testInstances() {
        val app = FirebaseApp.getInstance()
        assertEquals(functions, Firebase.functions(app))
        assertEquals(FirebaseFunctions.getInstance(), Firebase.functions)
        assertEquals(FirebaseFunctions.getInstance(app, "europe-west1"), Firebase.functions(app, "europe-west1"))
        assertEquals(FirebaseFunctions.getInstance("europe-west1"), Firebase.functions("europe-west1"))
    }

    @Test
    fun testCallWithTask() = runTest {
        val callable: HttpsCallableReference = functions.getHttpsCallable("throwHttpsError")
        val exception = assertFailsWith<FirebaseFunctionsException> {
            callable.call(mapOf("code" to "not-found")).await()
        }
        assertEquals(FirebaseFunctionsException.Code.NOT_FOUND, exception.code)
        assertTrue(exception.message?.contains("No data found from emulator") == true, "message was '${exception.message}'")
        val details = assertNotNull(exception.details)
        assertEquals("not-found", detailValue(details, "reason"))
        assertEquals(404, detailValue(details, "httpResponseCode").toString().toInt())
    }

    @Test
    fun testCallWithListeners() = runTest {
        val failure = TaskCompletionSource<Exception>()
        val callable = functions.getHttpsCallable("throwHttpsError")
        callable.call() // no data: the emulator function reports an internal error
            .addOnSuccessListener { result: HttpsCallableResult -> failure.setException(IllegalStateException("Expected a failure, got ${result.data}")) }
            .addOnFailureListener { failure.setResult(it) }
        val exception = failure.task.await()
        assertTrue(exception is FirebaseFunctionsException, "$exception")
        assertEquals(FirebaseFunctionsException.Code.INTERNAL, exception.code)
    }

    @Test
    fun testOptionsAndTimeouts() = runTest {
        val options = HttpsCallableOptions.Builder().setLimitedUseAppCheckTokens(true).build()
        assertTrue(options.limitedUseAppCheckTokens)
        assertTrue(options.getLimitedUseAppCheckTokens())
        assertFalse(HttpsCallableOptions.Builder().build().limitedUseAppCheckTokens)

        val callable = functions.getHttpsCallable("throwHttpsError") { setLimitedUseAppCheckTokens(false) }
        assertEquals(70_000, callable.timeout)
        val copy = callable.withTimeout(30.seconds)
        assertEquals(30_000, copy.timeout)
        callable.setTimeout(5.seconds)
        assertEquals(5_000, callable.timeout)
        val exception = assertFailsWith<FirebaseFunctionsException> { copy.call(mapOf("code" to "permission-denied")).await() }
        assertEquals(FirebaseFunctionsException.Code.PERMISSION_DENIED, exception.code)
    }

    @Test
    fun testCallableFromUrl() = runTest {
        val url = "http://$emulatorHost:5001/fir-kotlin-sdk/us-central1/throwHttpsError"
        val exception = assertFailsWith<FirebaseFunctionsException> {
            functions.getHttpsCallableFromUrl(url).call(mapOf("code" to "invalid-argument")).await()
        }
        assertEquals(FirebaseFunctionsException.Code.INVALID_ARGUMENT, exception.code)
        assertEquals("invalid-argument", detailValue(exception.details, "reason"))

        val withOptions = functions.getHttpsCallableFromUrl(url, HttpsCallableOptions.Builder().build())
        assertEquals(FirebaseFunctionsException.Code.NOT_FOUND, assertFailsWith<FirebaseFunctionsException> { withOptions.call(mapOf("code" to "not-found")).await() }.code)
        val withInit = functions.getHttpsCallableFromUrl(url) { setLimitedUseAppCheckTokens(false) }.withTimeout(20.seconds)
        assertEquals(20_000, withInit.timeout)
    }
}
