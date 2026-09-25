/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.HttpsCallableOptions
import com.google.firebase.functions.toFunctionsException
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FunctionsExceptionTest {

    @Test
    fun mapsFirebaseFunctionsNSError() {
        val details = mapOf<Any?, Any?>("httpResponseCode" to 404)
        val error = NSError.errorWithDomain(
            domain = "com.firebase.functions",
            code = 5,
            userInfo = mapOf<Any?, Any?>(
                NSLocalizedDescriptionKey to "No data found",
                "details" to details,
            ),
        )

        val exception = error.toFunctionsException()

        assertEquals(FunctionsExceptionCode.NOT_FOUND, exception.code)
        assertEquals("No data found", exception.message)
        assertEquals(details, exception.details)
    }

    @Test
    fun mapsCodes() {
        assertEquals(FirebaseFunctionsException.Code.NOT_FOUND, FirebaseFunctionsException.Code.fromValue(5))
        assertEquals(FirebaseFunctionsException.Code.UNKNOWN, FirebaseFunctionsException.Code.fromValue(99))
        assertEquals(FirebaseFunctionsException.Code.NOT_FOUND, FirebaseFunctionsException.Code.fromHttpStatus(404))
        assertEquals(FirebaseFunctionsException.Code.UNAUTHENTICATED, FirebaseFunctionsException.Code.fromHttpStatus(401))
        assertEquals(FirebaseFunctionsException.Code.UNKNOWN, FirebaseFunctionsException.Code.fromHttpStatus(418))
    }

    @Test
    fun buildsOptionsWithTheAndroidSdkDsl() {
        // The Android SDK's Builder exposes the flag as a public field, so its `init` blocks assign it.
        val options = HttpsCallableOptions.Builder().apply { limitedUseAppCheckTokens = true }.build()
        assertTrue(options.limitedUseAppCheckTokens)
    }
}
