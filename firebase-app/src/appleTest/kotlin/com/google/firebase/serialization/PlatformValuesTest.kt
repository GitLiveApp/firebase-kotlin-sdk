/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlinx.serialization.Serializable
import platform.Foundation.NSNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Serializable
data class WithNulls(val name: String? = "unset", val tags: List<String?> = emptyList())

/** A null inside a Foundation collection is an NSNull; the iOS SDK's values are read with it as null. */
class PlatformValuesTest {

    @Test
    fun readsNSNullAsNull() {
        val stored = mapOf("name" to NSNull.`null`(), "tags" to listOf("a", NSNull.`null`()))
        assertPlain(mapOf("name" to null, "tags" to listOf("a", null)), firestore.convertToPlainValue(stored))
        assertEquals(WithNulls(null, listOf("a", null)), firestore.decodeFromPlainValue<WithNulls>(stored))
        assertNull(firestore.decodeFromPlainValue<String?>(NSNull.`null`()))
    }
}
