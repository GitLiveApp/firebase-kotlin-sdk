/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import kotlinx.serialization.Serializable
import kotlin.js.JSON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Serializable
data class JsReply(val name: String, val count: Int, val tags: List<String>, val nested: JsNested)

@Serializable
data class JsNested(val flag: Boolean)

/** The values the JS SDK returns, as JSON.parse produces them, are read as plain values; plain values are written as JS values. */
class PlatformValuesTest {

    private val reply: Any = js("""({name: "a", count: 2, tags: ["x", "y"], nested: {flag: true, missing: undefined, nothing: null}})""")

    @Test
    fun readsTheJsSdksValuesAsPlainValues() {
        val expected = mapOf("name" to "a", "count" to 2, "tags" to listOf("x", "y"), "nested" to mapOf("flag" to true, "missing" to null, "nothing" to null))
        assertPlain(expected, functions.convertToPlainValue(reply))
        assertPlain(expected, functions.decodeFromPlainValue<Map<String, Any?>>(reply))
    }

    @Test
    fun decodesTheJsSdksValues() {
        assertEquals(JsReply("a", 2, listOf("x", "y"), JsNested(true)), functions.decodeFromPlainValue<JsReply>(reply))
        assertEquals("a", functions.decodeFromPlainValue<String>(js("'a'")))
        assertEquals(listOf(1, 2), functions.decodeFromPlainValue<List<Int>>(js("[1, 2]")))
    }

    @Test
    fun writesPlainValuesAsTheJsSdksValues() {
        val platform = functions.convertToPlatformValue(mapOf("a" to listOf(1L, 2.5), "b" to mapOf("c" to "d"), "e" to null))
        assertEquals("""{"a":[1,2.5],"b":{"c":"d"},"e":null}""", JSON.stringify(platform))
    }

    @Test
    fun rejectsOtherJsObjects() {
        assertFailsWith<IllegalArgumentException> { functions.convertToPlainValue(js("new Date(0)")) }
    }
}
