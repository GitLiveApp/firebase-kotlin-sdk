/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
data class TestData(val map: Map<String, String>, val bool: Boolean = false)

class EncodersTest {
    @Test
    fun `encode a map`() {
        val encoded = encode(mapOf("key" to "value"))
        assertEquals(mapOf("key" to "value"), encoded)
    }

    @Test
    fun `encode a class`() {
        val encoded = encode<TestData>(TestData::class.serializer(), TestData(mapOf("key" to "value"), true))
        assertEquals(mapOf("map" to mapOf("key" to "value"), "bool" to true), encoded)
    }

    @Test
    fun `decode a class`() {
        val decoded = decode<TestData>(TestData::class.serializer(), mapOf("map" to mapOf("key" to "value")))
        assertEquals(TestData(mapOf("key" to "value"), false), decoded)
    }

    @Test
    fun `decode a list of class`() {
        val decoded = decode(ListSerializer(TestData::class.serializer()), listOf(mapOf("map" to mapOf("key" to "value"))))
        assertEquals(listOf(TestData(mapOf("key" to "value"), false)), decoded)
    }
}