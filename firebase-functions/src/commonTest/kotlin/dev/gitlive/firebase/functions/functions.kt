/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

expect val emulatorHost: String
expect val context: Any
expect fun detailValue(details: Any?, key: String): Any?

class FirebaseFunctionsTest {

    data class ExpectedFunctionError(
        val code: String,
        val exceptionCode: FunctionsExceptionCode,
        val message: String,
        val httpResponseCode: Int,
    )

    private lateinit var firebaseApp: FirebaseApp
    private lateinit var functions: FirebaseFunctions

    @BeforeTest
    fun initializeFirebase() {
        val app = Firebase.apps(context).firstOrNull() ?: Firebase.initialize(
            context,
            FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
        )
        firebaseApp = app

        functions = Firebase.functions(app).apply {
            useEmulator(emulatorHost, 5001)
        }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    @Test
    fun mapsHttpsErrorsFromEmulator() = runTest {
        listOf(
            ExpectedFunctionError(
                code = "invalid-argument",
                exceptionCode = FunctionsExceptionCode.INVALID_ARGUMENT,
                message = "Invalid argument from emulator",
                httpResponseCode = 400,
            ),
            ExpectedFunctionError(
                code = "not-found",
                exceptionCode = FunctionsExceptionCode.NOT_FOUND,
                message = "No data found from emulator",
                httpResponseCode = 404,
            ),
            ExpectedFunctionError(
                code = "permission-denied",
                exceptionCode = FunctionsExceptionCode.PERMISSION_DENIED,
                message = "Permission denied from emulator",
                httpResponseCode = 403,
            ),
        ).forEach { expected ->
            val exception = assertFailsWith<FirebaseFunctionsException> {
                functions.httpsCallable("throwHttpsError")(mapOf("code" to expected.code))
            }

            assertEquals(expected.exceptionCode, exception.code)
            assertTrue(
                exception.message?.contains(expected.message) == true,
                "Expected message to contain '${expected.message}' but was '${exception.message}'",
            )

            val details = assertNotNull(exception.details)
            assertEquals(expected.code, detailValue(details, "reason"))
            assertEquals(expected.httpResponseCode, detailValue(details, "httpResponseCode").toString().toInt())
        }
    }
}
