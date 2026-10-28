/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlinx.serialization.Serializable
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSNull
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
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
        assertEquals("{\"on\":true}", json(functions.convertToPlainValue(mapOf("on" to true))))
        assertEquals("[false,1]", json(database.convertToPlainValue(listOf(false, 1))))
    }

    @Test
    fun readsFoundationBooleansAsBooleans() {
        val stored = mapOf("on" to NSNumber.numberWithBool(true), "count" to 1)
        assertPlain(mapOf("on" to true, "count" to 1), firestore.convertToPlainValue(stored))
        assertEquals(true, firestore.decodeFromPlainValue<Boolean>(NSNumber.numberWithBool(true)))
        assertEquals(true, toKotlin(functions.convertToPlainValue(true)))
    }

    private fun json(value: Any?): String {
        val data = NSJSONSerialization.dataWithJSONObject(value!!, 0u, null)!!
        return NSString.create(data = data, encoding = NSUTF8StringEncoding).toString()
    }
}
