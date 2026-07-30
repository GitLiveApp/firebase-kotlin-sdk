/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.internal

import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Serializes with NSJSONSerialization, the same Foundation serialization the Firebase SDKs
 * use, so a Kotlin Boolean that lost its CFBoolean identity shows up as 1/0 here exactly
 * like it would in the database.
 */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
private fun foundationJson(value: Any): String {
    val data = NSJSONSerialization.dataWithJSONObject(value, 0u, null)!!
    return NSString.create(data = data, encoding = NSUTF8StringEncoding) as String
}

class FoundationValueTest {

    @Test
    fun mapBooleansSurviveFoundationSerialization() {
        val transformed = mapOf("v" to true).withFoundationBooleans()!!
        assertEquals("""{"v":true}""", foundationJson(transformed))
    }

    @Test
    fun untransformedMapBooleansAreLost() {
        assertEquals("""{"v":1}""", foundationJson(mapOf("v" to true)))
    }

    @Test
    fun listBooleansSurviveFoundationSerialization() {
        val transformed = listOf(true, false, "x").withFoundationBooleans()!!
        assertEquals("""[true,false,"x"]""", foundationJson(transformed))
    }

    @Test
    fun nestedContainersAreTransformed() {
        val transformed = mapOf("nested" to mapOf("list" to listOf(false))).withFoundationBooleans()!!
        assertEquals("""{"nested":{"list":[false]}}""", foundationJson(transformed))
    }

    @Test
    fun nonBooleanValuesArePreserved() {
        val transformed = mapOf("all" to listOf(true, 42L, 9007199254740993L, 1.5, "a\"b\n\\c")).withFoundationBooleans()!!
        assertEquals("""{"all":[true,42,9007199254740993,1.5,"a\"b\n\\c"]}""", foundationJson(transformed))
    }

    @Test
    fun booleanFreeContainersAreReturnedUnchanged() {
        val original = mapOf("n" to 1, "s" to "x", "nested" to listOf(1, 2))
        assertSame(original, original.withFoundationBooleans())
    }

    @Test
    fun scalarsAreReturnedUnchanged() {
        assertEquals(true, true.withFoundationBooleans())
        assertEquals("x", "x".withFoundationBooleans())
        assertEquals(null, null.withFoundationBooleans())
    }

    @Test
    fun unrepresentableContentFallsBackToOriginal() {
        val withCustomObject = mapOf("v" to true, "bad" to Any())
        assertSame(withCustomObject, withCustomObject.withFoundationBooleans())

        val withNonStringKey = mapOf(1 to true)
        assertSame(withNonStringKey, withNonStringKey.withFoundationBooleans())

        val withNonFiniteDouble = mapOf("v" to true, "d" to Double.NaN)
        assertSame(withNonFiniteDouble, withNonFiniteDouble.withFoundationBooleans())
    }

    @Test
    fun serverValueStyleSentinelsAreSupported() {
        val transformed = mapOf("flag" to true, "ts" to mapOf(".sv" to "timestamp")).withFoundationBooleans()!!
        val json = foundationJson(transformed)
        assertTrue("\"flag\":true" in json, json)
        assertTrue("""{".sv":"timestamp"}""" in json, json)
    }
}
