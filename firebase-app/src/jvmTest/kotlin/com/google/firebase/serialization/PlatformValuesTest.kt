/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** The Android SDK and firebase-java-sdk exchange Kotlin collections, so the plain values are the Kotlin ones. */
class PlatformValuesTest {

    @Test
    fun producesKotlinCollections() {
        val encoded = functions.encodeToPlainValue(Score("a", 1.5, true))
        assertIs<HashMap<*, *>>(encoded)
        assertEquals(hashMapOf("player" to "a", "points" to 1.5, "isWinner" to true), encoded)
        assertIs<ArrayList<*>>(functions.convertToPlainValue(listOf(1)))
    }
}
