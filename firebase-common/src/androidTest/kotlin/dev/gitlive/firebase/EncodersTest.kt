/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Serializable
data class TestData(val map: Map<String, String>, val bool: Boolean = false, val nullableBool: Boolean? = null)

class EncodersTest {
    @Test
    fun encodeMap() {
        val encoded = encode(mapOf("key" to "value"), shouldEncodeElementDefault = true)
        assertEquals(mapOf("key" to "value"), encoded)
    }

    @Test
    fun encodeObject() {
        val encoded = encode<TestData>(TestData::class.serializer(), TestData(mapOf("key" to "value"), true), shouldEncodeElementDefault = false)
        assertEquals(mapOf("map" to mapOf("key" to "value"), "bool" to true), encoded)
    }

    @Test
    fun decodeObject() {
        val decoded = decode<TestData>(TestData::class.serializer(), mapOf("map" to mapOf("key" to "value")))
        assertEquals(TestData(mapOf("key" to "value"), false), decoded)
    }

    @Test
    fun decodeListOfObjects() {
        val decoded = decode(ListSerializer(TestData::class.serializer()), listOf(mapOf("map" to mapOf("key" to "value"))))
        assertEquals(listOf(TestData(mapOf("key" to "value"), false)), decoded)
    }

    @Test
    fun decodeObjectNullableValue() {
        val decoded = decode<TestData>(TestData::class.serializer(), mapOf("map" to mapOf("key" to "value"), "nullableBool" to null))
        assertNull(decoded.nullableBool)
    }
}