/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.functions

import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import kotlin.test.Test
import kotlin.test.assertEquals

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

        val exception = error.toException()

        assertEquals(FunctionsExceptionCode.NOT_FOUND, exception.code)
        assertEquals("No data found", exception.message)
        assertEquals(details, exception.details)
    }
}
