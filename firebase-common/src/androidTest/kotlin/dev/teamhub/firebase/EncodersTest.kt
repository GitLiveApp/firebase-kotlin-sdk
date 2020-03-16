package dev.teamhub.firebase

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
data class TestData(val map: Map<String, String>)

class EncodersTest {
    @Test
    fun `encode a map`() {
        val encoded = encode<TestData>(TestData::class.serializer(), TestData(mapOf("key" to "value")))
        assertEquals(mapOf("map" to mapOf("key" to "value")), encoded)
    }

    @Test
    fun `decode a map`() {
        val decoded = decode<TestData>(TestData::class.serializer(), mapOf("map" to mapOf("key" to "value")))
        assertEquals(TestData(mapOf("key" to "value")), decoded)
    }}