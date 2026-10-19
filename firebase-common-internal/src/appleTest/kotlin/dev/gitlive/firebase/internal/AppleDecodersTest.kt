/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.internal

import kotlinx.serialization.Serializable
import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSNumber
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Serializable
data class NumberData(
    val double: Double = 0.0,
    val float: Float = 0f,
    val long: Long = 0,
    val int: Int = 0,
    val short: Short = 0,
    val byte: Byte = 0,
    val bool: Boolean = false,
    val nullableDouble: Double? = null,
)

class AppleDecodersTest {

    @Test
    fun decodeNSDecimalNumberAsDouble() {
        val decoded = decode<NumberData>(mapOf("double" to NSDecimalNumber("0.029661016538739204")))
        assertEquals(0.029661016538739204, decoded.double)
    }

    @Test
    fun decodeNSDecimalNumberAsNullableDouble() {
        val decoded = decode<NumberData>(mapOf("nullableDouble" to NSDecimalNumber("0.029661016538739204")))
        assertEquals(0.029661016538739204, decoded.nullableDouble)
    }

    @Test
    fun decodeNullAsNullableDouble() {
        val decoded = decode<NumberData>(mapOf("nullableDouble" to null))
        assertNull(decoded.nullableDouble)
    }

    @Test
    fun decodeNSDecimalNumberAsWholeNumbers() {
        val decoded = decode<NumberData>(
            mapOf(
                "long" to NSDecimalNumber("9007199254740993"),
                "int" to NSDecimalNumber("42"),
                "short" to NSDecimalNumber("7"),
                "byte" to NSDecimalNumber("3"),
                "float" to NSDecimalNumber("1.5"),
                "bool" to NSDecimalNumber("1"),
            ),
        )
        assertEquals(9007199254740993L, decoded.long)
        assertEquals(42, decoded.int)
        assertEquals(7, decoded.short)
        assertEquals(3, decoded.byte)
        assertEquals(1.5f, decoded.float)
        assertEquals(true, decoded.bool)
    }

    @Test
    fun decodeTopLevelNSDecimalNumber() {
        assertEquals(0.029661016538739204, decode<Double>(NSDecimalNumber("0.029661016538739204")))
        assertEquals(42L, decode<Long>(NSDecimalNumber("42")))
    }

    @Test
    fun decodePlainNSNumber() {
        val decoded = decode<NumberData>(
            mapOf(
                "double" to NSNumber(double = 0.5),
                "long" to NSNumber(longLong = 12L),
                "nullableDouble" to NSNumber(double = 0.25),
            ),
        )
        assertEquals(0.5, decoded.double)
        assertEquals(12L, decoded.long)
        assertEquals(0.25, decoded.nullableDouble)
    }
}
