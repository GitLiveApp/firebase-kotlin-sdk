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
import kotlinx.serialization.modules.polymorphic
import kotlin.jvm.JvmInline
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
object TestObject {
    val map = mapOf("key" to "value", "key2" to 12, "key3" to null)
    val bool = false
    val nullableBool: Boolean? = null
}

@Serializable
data class TestData(val map: Map<String, String>, val otherMap: Map<Int, Int>, val bool: Boolean = false, val nullableBool: Boolean? = null)

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

@Serializable
data class NestedClass(
    val sealed: SealedClass,
    val abstract: AbstractClass,
    val sealedList: List<SealedClass>,
    val abstractList: List<AbstractClass>,
    val sealedMap: Map<SealedClass, SealedClass>,
    val abstractMap: Map<AbstractClass, AbstractClass>
)

@Serializable
@JvmInline
value class ValueClass(val int: Int)

@Serializable
data class ValueClassWrapper(val value: ValueClass)

class EncodersTest {

    @Test
    fun encodeDecodeList() {
        val list = listOf("One", "Two", "Three")
        val encoded = encode<List<String>>(list) { encodeDefaults = true }

        nativeAssertEquals(nativeListOf("One", "Two", "Three"), encoded)

        val decoded = decode(ListSerializer(String.serializer()), encoded)
        assertEquals(listOf("One", "Two", "Three"), decoded)
    }

    @Test
    fun encodeDecodeMap() {
        val map = mapOf("key" to "value", "key2" to "value2", "key3" to "value3")
        val encoded = encode<Map<String, String>>(map) { encodeDefaults = true }

        nativeAssertEquals(nativeMapOf("key" to "value", "key2" to "value2", "key3" to "value3"), encoded)

        val decoded = decode(MapSerializer(String.serializer(), String.serializer()), encoded)
        assertEquals(mapOf("key" to "value", "key2" to "value2", "key3" to "value3"), decoded)
    }

    @Test
    fun encodeDecodeObject() {
        val encoded = encode(TestObject.serializer(), TestObject) { encodeDefaults = false }
        nativeAssertEquals(nativeMapOf(), encoded)

        val decoded = decode(TestObject.serializer(), encoded)
        assertEquals(TestObject, decoded)
    }

    @Test
    fun encodeDecodeClass() {
        val testDataClass = TestData(mapOf("key" to "value"), mapOf(1 to 1), true)
        val encoded = encode(TestData.serializer(), testDataClass) { encodeDefaults = false }

        nativeAssertEquals(nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true), encoded)

        val decoded = decode(TestData.serializer(), encoded)
        assertEquals(testDataClass, decoded)
    }

    @Test
    fun encodeDecodeClassNullableValue() {
        val testDataClass = TestData(mapOf("key" to "value"), mapOf(1 to 1), true, nullableBool = true)
        val encoded = encode(TestData.serializer(), testDataClass) { encodeDefaults = true }

        nativeAssertEquals(nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "nullableBool" to true), encoded)

        val decoded = decode(TestData.serializer(), encoded)
        assertEquals(testDataClass, decoded)
    }

    @Test
    fun encodeDecodeGenericClass() {
        val innerClass = TestData(mapOf("key" to "value"), mapOf(1 to 1), true)
        val genericClass = GenericClass(innerClass)
        val encoded = encode(GenericClass.serializer(TestData.serializer()), genericClass) { encodeDefaults = true }

        nativeAssertEquals(nativeMapOf("inner" to nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "nullableBool" to null)), encoded)

        val decoded = decode(GenericClass.serializer(TestData.serializer()), encoded)
        assertEquals(genericClass, decoded)
    }

    @Test
    fun encodeDecodeSealedClass() {
        val sealedClass = SealedClass.Test("value")
        val encoded = encode(SealedClass.serializer(), sealedClass) { encodeDefaults = true }

        nativeAssertEquals(nativeMapOf("type" to "test", "value" to "value"), encoded)

        val decoded = decode(SealedClass.serializer(), encoded)
        assertEquals(sealedClass, decoded)
    }

    @Test
    fun encodeDecodePolymorphicClass() {
        val module = SerializersModule {
            polymorphic(AbstractClass::class, AbstractClass.serializer()) {
                subclass(ImplementedClass::class, ImplementedClass.serializer())
            }
        }
        val abstractClass: AbstractClass = ImplementedClass("value", true)
        val encoded =
            encode(AbstractClass.serializer(), abstractClass) {
                encodeDefaults = true
                serializersModule = module
            }

        nativeAssertEquals(nativeMapOf("type" to "implemented", "value" to "value", "otherValue" to true), encoded)

        val decoded = decode(AbstractClass.serializer(), encoded) {
            serializersModule = module
        }
        assertEquals(abstractClass, decoded)
    }

    @Test
    fun encodeDecodeNestedClass() {
        val module = SerializersModule {
            polymorphic(AbstractClass::class, AbstractClass.serializer()) {
                subclass(ImplementedClass::class, ImplementedClass.serializer())
            }
        }

        val sealedClass: SealedClass = SealedClass.Test("value")
        val abstractClass: AbstractClass = ImplementedClass("value", true)
        val nestedClass = NestedClass(sealedClass, abstractClass, listOf(sealedClass), listOf(abstractClass), mapOf(sealedClass to sealedClass), mapOf(abstractClass to abstractClass))
        val encoded = encode(NestedClass.serializer(), nestedClass) {
            encodeDefaults = true
            serializersModule = module
        }

        val sealedEncoded = nativeMapOf("type" to "test", "value" to "value")
        val abstractEncoded = nativeMapOf("type" to "implemented", "value" to "value", "otherValue" to true)
        nativeAssertEquals(
            nativeMapOf(
                "sealed" to sealedEncoded,
                "abstract" to abstractEncoded,
                "sealedList" to nativeListOf(sealedEncoded),
                "abstractList" to nativeListOf(abstractEncoded),
                "sealedMap" to nativeMapOf(sealedEncoded to sealedEncoded),
                "abstractMap" to nativeMapOf(abstractEncoded to abstractEncoded)
            ),
            encoded
        )

        val decoded = decode(NestedClass.serializer(), encoded) {
            serializersModule = module
        }
        assertEquals(nestedClass, decoded)
    }

    @Test
    fun encodeDecodeValueClassWrapper() {
        val testValueClassWrapper = ValueClassWrapper(ValueClass(42))
        val encoded = encode(ValueClassWrapper.serializer(), testValueClassWrapper) { encodeDefaults = false }

        nativeAssertEquals(nativeMapOf("value" to 42), encoded)

        val decoded = decode(ValueClassWrapper.serializer(), encoded)
        assertEquals(testValueClassWrapper, decoded)
    }

    @Test
    fun encodeDecodeValueClass() {
        val testValueClass = ValueClass(42)
        val encoded = encode(ValueClass.serializer(), testValueClass) { encodeDefaults = false }

        nativeAssertEquals(42, encoded)

        val decoded = decode(ValueClass.serializer(), encoded)
        assertEquals(testValueClass, decoded)
    }

    @Test
    fun reencodeTransformationList() {
        val reencoded = reencodeTransformation<List<String>>(nativeListOf("One", "Two", "Three")) {
            assertEquals(listOf("One", "Two", "Three"), it)
            it.map { value -> "new$value" }
        }
        nativeAssertEquals(nativeListOf("newOne", "newTwo", "newThree"), reencoded)
    }

    @Test
    fun reencodeTransformationMap() {
        val reencoded = reencodeTransformation<Map<String, String>>(nativeMapOf("key" to "value", "key2" to "value2", "key3" to "value3")) {
            assertEquals(mapOf("key" to "value", "key2" to "value2", "key3" to "value3"), it)
            it.mapValues { (_, value) -> "new-$value" }
        }

        nativeAssertEquals(nativeMapOf("key" to "new-value", "key2" to "new-value2", "key3" to "new-value3"), reencoded)
    }

    @Test
    fun reencodeTransformationObject() {
        val reencoded = reencodeTransformation<TestObject>(nativeMapOf(), { encodeDefaults = false }) {
            assertEquals(TestObject, it)
            it
        }
        nativeAssertEquals(nativeMapOf(), reencoded)
    }

    @Test
    fun reencodeTransformationClass() {
        val reencoded = reencodeTransformation<TestData>(
            nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "nullableBool" to true),
            { encodeDefaults = false }
        ) {
            assertEquals(TestData(mapOf("key" to "value"), mapOf(1 to 1), bool = true, nullableBool = true), it)
            it.copy(map = mapOf("newKey" to "newValue"), nullableBool = null)
        }

        nativeAssertEquals(nativeMapOf("map" to nativeMapOf("newKey" to "newValue"), "otherMap" to nativeMapOf(1 to 1), "bool" to true), reencoded)
    }

    @Test
    fun reencodeTransformationNullableValue() {
        val reencoded = reencodeTransformation<TestData?>(
            nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "nullableBool" to true),
            { encodeDefaults = false }
        ) {
            assertEquals(TestData(mapOf("key" to "value"), mapOf(1 to 1), bool = true, nullableBool = true), it)
            null
        }

        nativeAssertEquals(null, reencoded)
    }

    @Test
    fun reencodeTransformationGenericClass() {
        val reencoded = reencodeTransformation(
            GenericClass.serializer(TestData.serializer()),
            nativeMapOf("inner" to nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "nullableBool" to false)),
            { encodeDefaults = false }
        ) {
            assertEquals(
                GenericClass(TestData(mapOf("key" to "value"), mapOf(1 to 1), bool = true, nullableBool = false)),
                it
            )
            GenericClass(it.inner.copy(map = mapOf("newKey" to "newValue"), nullableBool = null))
        }

        nativeAssertEquals(nativeMapOf("inner" to nativeMapOf("map" to nativeMapOf("newKey" to "newValue"), "otherMap" to nativeMapOf(1 to 1), "bool" to true)), reencoded)
    }

    @Test
    fun reencodeTransformationSealedClass() {
        val reencoded = reencodeTransformation(SealedClass.serializer(), nativeMapOf("type" to "test", "value" to "value")) {
            assertEquals(SealedClass.Test("value"), it)
            SealedClass.Test("newTest")
        }

        nativeAssertEquals(nativeMapOf("type" to "test", "value" to "newTest"), reencoded)
    }

    @Test
    fun reencodeTransformationPolymorphicClass() {
        val module = SerializersModule {
            polymorphic(AbstractClass::class, AbstractClass.serializer()) {
                subclass(ImplementedClass::class, ImplementedClass.serializer())
            }
        }

        val reencoded = reencodeTransformation(
            AbstractClass.serializer(),
            nativeMapOf("type" to "implemented", "value" to "value", "otherValue" to true),
            builder = {
                serializersModule = module
            }
        ) {
            assertEquals(ImplementedClass("value", true), it)
            ImplementedClass("new-${it.value}", false)
        }

        nativeAssertEquals(nativeMapOf("type" to "implemented", "value" to "new-value", "otherValue" to false), reencoded)
    }

    @Test
    fun reencodeTransformationNestedClass() {
        val module = SerializersModule {
            polymorphic(AbstractClass::class, AbstractClass.serializer()) {
                subclass(ImplementedClass::class, ImplementedClass.serializer())
            }
        }

        val sealedClass: SealedClass = SealedClass.Test("value")
        val abstractClass: AbstractClass = ImplementedClass("value", true)
        val nestedClass = NestedClass(sealedClass, abstractClass, listOf(sealedClass), listOf(abstractClass), mapOf(sealedClass to sealedClass), mapOf(abstractClass to abstractClass))
        val encoded = encode(NestedClass.serializer(), nestedClass) {
            encodeDefaults = true
            serializersModule = module
        }

        val reencoded = reencodeTransformation(NestedClass.serializer(), encoded, builder = {
            encodeDefaults = true
            serializersModule = module
        }) {
            assertEquals(nestedClass, it)
            it.copy(sealed = SealedClass.Test("newValue"))
        }

        val sealedEncoded = nativeMapOf("type" to "test", "value" to "value")
        val abstractEncoded = nativeMapOf("type" to "implemented", "value" to "value", "otherValue" to true)
        nativeAssertEquals(
            nativeMapOf(
                "sealed" to nativeMapOf("type" to "test", "value" to "newValue"),
                "abstract" to abstractEncoded,
                "sealedList" to nativeListOf(sealedEncoded),
                "abstractList" to nativeListOf(abstractEncoded),
                "sealedMap" to nativeMapOf(sealedEncoded to sealedEncoded),
                "abstractMap" to nativeMapOf(abstractEncoded to abstractEncoded)
            ),
            reencoded
        )
    }
}
