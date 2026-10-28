/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/** The Android SDK and firebase-java-sdk exchange plain values as they are. */
class PlatformValuesTest {

    @Test
    fun passesPlainValuesThroughUnchanged() {
        val value = hashMapOf("a" to arrayListOf(1L, 2.5), "b" to hashMapOf("c" to "d"))
        assertSame(value, functions.convertToPlatformValue(value))
        assertEquals(value, functions.convertToPlainValue(value))
    }
}
