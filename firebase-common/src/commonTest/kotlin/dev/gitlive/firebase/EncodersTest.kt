/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals

expect fun nativeMapOf(vararg pairs: Pair<String, Any?>): Any
expect fun nativeListOf(vararg elements: Any): Any
expect fun nativeAssertEquals(expected: Any?, actual: Any?): Unit

@Serializable
object TestObject {
    val map = mapOf("key" to "value", "key2" to 12, "key3" to null)
    val bool = false
    val nullableBool: Boolean? = null
}

@Serializable
data class TestData(val map: Map<String, String>, val bool: Boolean = false, val nullableBool: Boolean? = null)

@Serializable
sealed class SealedClass {
    @Serializable
    @SerialName("test")
    data class Test(val value: String) : SealedClass()
}

@Serializable
data class GenericClass<T>(
    val inner: T
)

@Serializable
abstract class AbstractClass {
    abstract val value: String
}

@Serializable
@SerialName("implemented")
data class ImplementedClass(override val value: String, val otherValue: Boolean) : AbstractClass()

class EncodersTest {

    @Test
    fun encodeDecodeList() {
        val list = listOf("One", "Two", "Three")
        val encoded = encode(list, shouldEncodeElementDefault = true)

        nativeAssertEquals(nativeListOf("One", "Two", "Three"), encoded)

        val decoded = decode(ListSerializer(String.serializer()), encoded)
        assertEquals(listOf("One", "Two", "Three"), decoded)
    }

    @Test
    fun encodeDecodeMap() {
        val map = mapOf("key" to "value", "key2" to "value2", "key3" to "value3")
        val encoded = encode(map, shouldEncodeElementDefault = true)

        nativeAssertEquals(nativeMapOf("key" to "value", "key2" to "value2", "key3" to "value3"), encoded)

        val decoded = decode(MapSerializer(String.serializer(), String.serializer()), encoded)
        assertEquals(mapOf("key" to "value", "key2" to "value2", "key3" to "value3"), decoded)
    }

    @Test
    fun encodeDecodeObject() {
        val encoded = encode(TestObject.serializer(), TestObject, shouldEncodeElementDefault = false)
        nativeAssertEquals(nativeMapOf(), encoded)

        val decoded = decode(TestObject.serializer(), encoded)
        assertEquals(TestObject, decoded)
    }

    @Test
    fun encodeDecodeClass() {
        val testDataClass = TestData(mapOf("key" to "value"), true)
        val encoded = encode(TestData.serializer(), testDataClass, shouldEncodeElementDefault = false)

        nativeAssertEquals(nativeMapOf("map" to nativeMapOf("key" to "value"), "bool" to true), encoded)

        val decoded = decode(TestData.serializer(), encoded)
        assertEquals(testDataClass, decoded)
    }

    @Test
    fun encodeDecodeClassNullableValue() {
        val testDataClass = TestData(mapOf("key" to "value"), true, nullableBool = true)
        val encoded = encode(TestData.serializer(), testDataClass, shouldEncodeElementDefault = true)

        nativeAssertEquals(nativeMapOf("map" to nativeMapOf("key" to "value"), "bool" to true, "nullableBool" to true), encoded)

        val decoded = decode(TestData.serializer(), encoded)
        assertEquals(testDataClass, decoded)
    }

    @Test
    fun encodeDecodeGenericClass() {
        val innerClass = TestData(mapOf("key" to "value"), true)
        val genericClass = GenericClass(innerClass)
        val encoded = encode(GenericClass.serializer(TestData.serializer()), genericClass, shouldEncodeElementDefault = true)

        nativeAssertEquals(nativeMapOf("inner" to nativeMapOf("map" to nativeMapOf("key" to "value"), "bool" to true, "nullableBool" to null)), encoded)

        val decoded = decode(GenericClass.serializer(TestData.serializer()), encoded)
        assertEquals(genericClass, decoded)
    }

    @Test
    fun encodeDecodeSealedClass() {
        val sealedClass = SealedClass.Test("value")
        val encoded = encode(SealedClass.serializer(), sealedClass, shouldEncodeElementDefault = true)

        nativeAssertEquals(nativeListOf("test", nativeMapOf("value" to "value")), encoded)

        val decoded = decode(SealedClass.serializer(), encoded)
        assertEquals(sealedClass, decoded)
    }

    @Test
    fun encodeDecodePolymorphicClass() {
        val module = SerializersModule {
            polymorphic(AbstractClass::class, ImplementedClass::class, ImplementedClass.serializer())
        }
        val abstractClass: AbstractClass = ImplementedClass("value", true)
        val encoded = encode(AbstractClass.serializer(), abstractClass, shouldEncodeElementDefault = true, module)

        nativeAssertEquals(nativeListOf("implemented", nativeMapOf("value" to "value", "otherValue" to true)), encoded)

        val decoded = decode(AbstractClass.serializer(), encoded, module)
        assertEquals(abstractClass, decoded)
    }
}
