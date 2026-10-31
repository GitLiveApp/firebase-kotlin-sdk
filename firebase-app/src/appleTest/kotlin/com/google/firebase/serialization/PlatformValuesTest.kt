/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlinx.cinterop.BetaInteropApi
import kotlinx.serialization.Serializable
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSNull
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Serializable
data class WithNulls(val name: String? = "unset", val tags: List<String?> = emptyList())

@Serializable
data class Flags(val on: Boolean, val off: Boolean, val count: Int)

/**
 * A null inside a Foundation collection is an NSNull, and a boolean the format writes is Foundation's own, which the iOS
 * SDKs' JSON serialization writes as a boolean; both are read back as the Kotlin value.
 */
class PlatformValuesTest {

    @Test
    fun readsNSNullAsNull() {
        val stored = mapOf("name" to NSNull.`null`(), "tags" to listOf("a", NSNull.`null`()))
        assertPlain(mapOf("name" to null, "tags" to listOf("a", null)), firestore.convertToPlainValue(stored))
        assertEquals(WithNulls(null, listOf("a", null)), firestore.decodeFromPlainValue<WithNulls>(stored))
        assertNull(firestore.decodeFromPlainValue<String?>(NSNull.`null`()))
        assertNull(firestore.convertToCustomClass(NSNull.`null`(), WithNulls::class))
    }

    @Test
    fun writesBooleansAsFoundationBooleans() {
        val encoded = functions.encodeToPlainValue(Flags(on = true, off = false, count = 1))
        assertPlain(mapOf("on" to true, "off" to false, "count" to 1), encoded)
        assertEquals(Flags(true, false, 1), functions.decodeFromPlainValue(encoded))
        assertEquals("{\"count\":1,\"off\":false,\"on\":true}", json(encoded))
        val nested = mapOf("a" to listOf(true, mapOf("b" to false), null), "n" to null, "s" to "x", "d" to 1.5)
        assertEquals("{\"a\":[true,{\"b\":false},null],\"d\":1.5,\"n\":null,\"s\":\"x\"}", json(database.convertToPlainValue(nested)))
    }

    @Test
    fun readsFoundationBooleansAsBooleans() {
        val stored = parse("{\"on\":true,\"off\":false,\"count\":1,\"nested\":{\"flags\":[true,null]}}")
        assertPlain(mapOf("on" to true, "off" to false, "count" to 1, "nested" to mapOf("flags" to listOf(true, null))), stored)
        assertEquals(Flags(true, false, 1), firestore.decodeFromPlainValue(stored))
        assertEquals(true, firestore.decodeFromPlainValue<Boolean>(parse("true")))
        val reused = functions.convertToPlainValue(mapOf("on" to true, "nested" to (stored as Map<*, *>)["nested"]))
        assertEquals("{\"nested\":{\"flags\":[true,null]},\"on\":true}", json(reused))
    }

    @OptIn(BetaInteropApi::class)
    private fun json(value: Any?): String {
        val data = NSJSONSerialization.dataWithJSONObject(value!!, SORTED_KEYS, null)!!
        return NSString.create(data = data, encoding = NSUTF8StringEncoding).toString()
    }

    @OptIn(BetaInteropApi::class)
    private fun parse(json: String): Any? {
        val data = NSString.create(string = json).dataUsingEncoding(NSUTF8StringEncoding)!!
        return NSJSONSerialization.JSONObjectWithData(data, FRAGMENTS_ALLOWED, null)
    }

    private companion object {
        /** NSJSONWritingSortedKeys. */
        const val SORTED_KEYS = 2uL

        /** NSJSONReadingFragmentsAllowed. */
        const val FRAGMENTS_ALLOWED = 4uL
    }
}
