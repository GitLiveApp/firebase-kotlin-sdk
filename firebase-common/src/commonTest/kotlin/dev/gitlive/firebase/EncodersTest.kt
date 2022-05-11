/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

expect fun nativeMapOf(vararg pairs: Pair<String, Any?>): Any
expect fun nativeListOf(vararg elements: Any): Any
expect fun nativeAssertEquals(expected: Any?, actual: Any?): Unit

@Serializable
data class TestData(val map: Map<String, String>, val bool: Boolean = false, val nullableBool: Boolean? = null)

@Serializable
sealed class SealedClass {
    @Serializable
    @SerialName("test")
    data class Test(val value: String) : SealedClass()
}

@Serializable
data class GenericClass<T : SealedClass>(
    val inner: T
)

class EncodersTest {
    @Test
    fun encodeMap() {
        val encoded = encode(mapOf("key" to "value", "key2" to 12, "key3" to null), shouldEncodeElementDefault = true)

        nativeAssertEquals(nativeMapOf("key" to "value", "key2" to 12, "key3" to null), encoded)
    }

    @Test
    fun encodeObject() {
        val encoded = encode<TestData>(TestData.serializer(), TestData(mapOf("key" to "value"), true), shouldEncodeElementDefault = false)
        nativeAssertEquals(nativeMapOf("map" to nativeMapOf("key" to "value"), "bool" to true), encoded)
    }

    @Test
    fun encodeObjectNullableValue() {
        val encoded = encode<TestData>(TestData.serializer(), TestData(mapOf("key" to "value"), true, nullableBool = true), shouldEncodeElementDefault = true)
        nativeAssertEquals(nativeMapOf("map" to nativeMapOf("key" to "value"), "bool" to true, "nullableBool" to true), encoded)
    }

    @Test
    fun decodeObject() {
        val decoded = decode<TestData>(TestData.serializer(), nativeMapOf("map" to nativeMapOf("key" to "value")))
        assertEquals(TestData(mapOf("key" to "value"), false), decoded)
    }

    @Test
    fun decodeListOfObjects() {
        val decoded = decode(ListSerializer(TestData.serializer()), nativeListOf(nativeMapOf("map" to nativeMapOf("key" to "value"))))
        assertEquals(listOf(TestData(mapOf("key" to "value"), false)), decoded)
    }

    @Test
    fun decodeObjectNullableValue() {
        val decoded = decode(TestData.serializer(), nativeMapOf("map" to mapOf("key" to "value"), "nullableBool" to null))
        assertNull(decoded.nullableBool)
    }

    @Test
    fun testEncodeDecodedSealedClass() {
        val test = SealedClass.Test("Foo")
        val serializer = SealedClass.Test.serializer() // has to be used because of JS issue
        val encoded = encode(serializer, test, false)
        val decoded = decode(serializer, encoded)
        assertEquals(test, decoded)
    }

    @Test
    fun testEncodeDecodeGenericClass() {
        val test = SealedClass.Test("Foo")
        val generic = GenericClass(test)
        val serializer = GenericClass.serializer(SealedClass.Test.serializer())
        val encoded = encode(serializer, generic, false)
        val decoded = decode(serializer, encoded)
        assertEquals(generic, decoded)
    }
}
