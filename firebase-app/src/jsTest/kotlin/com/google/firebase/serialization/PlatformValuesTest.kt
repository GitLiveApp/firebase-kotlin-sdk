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

/** The JS SDK exchanges JS values: they are read as plain values, and the plain values produced are JS values. */
class PlatformValuesTest {
    private val reply: Any = js("""({name: "a", count: 2, tags: ["x", "y"], nested: {flag: true, missing: undefined, nothing: null}})""")
    private fun jsString(): Any = js("'a'")
    private fun jsArray(): Any = js("[1, 2]")
    private fun jsDate(): Any = js("new Date(0)")
    private fun reply(): Any = reply

    @Test
    fun readsTheJsSdksValuesAsPlainValues() {
        val expected = mapOf("name" to "a", "count" to 2, "tags" to listOf("x", "y"), "nested" to mapOf("flag" to true, "missing" to null, "nothing" to null))
        assertPlain(expected, functions.convertToPlainValue(reply()))
        assertEquals(expected, functions.decodeFromPlainValue<Map<String, Any?>>(reply()))
        assertEquals(expected, toKotlin(reply()))
    }

    @Test
    fun decodesTheJsSdksValues() {
        assertEquals(JsReply("a", 2, listOf("x", "y"), JsNested(true)), functions.decodeFromPlainValue<JsReply>(reply()))
        assertEquals("a", functions.decodeFromPlainValue<String>(jsString()))
        assertEquals(listOf(1, 2), functions.decodeFromPlainValue<List<Int>>(jsArray()))
    }

    @Test
    fun producesTheJsSdksValues() {
        val encoded = functions.encodeToPlainValue(JsReply("a", 2, listOf("x", "y"), JsNested(true)))
        assertEquals("""{"name":"a","count":2,"tags":["x","y"],"nested":{"flag":true}}""", JSON.stringify(encoded))
        val converted = firestore.convertToPlainValue(mapOf("a" to listOf(1L, 2.5), "b" to mapOf("c" to "d"), "e" to null, "__proto__" to 1))
        assertEquals("""{"a":[1,2.5],"b":{"c":"d"},"e":null,"__proto__":1}""", JSON.stringify(converted))
        assertEquals("""{"name":"a","count":2,"tags":["x","y"],"nested":{"flag":true,"missing":null,"nothing":null}}""", JSON.stringify(functions.convertToPlainValue(reply())))
    }

    @Test
    fun writesRootValuesAndLongsAsTheJsSdkTakesThem() {
        assertEquals("\"a\"", JSON.stringify(functions.encodeToPlainValue("a")))
        assertEquals("5", JSON.stringify(firestore.convertToPlainValue(5L)))
        // The JS Functions SDK sends a number, so the format writes the Int64Value wrapper the callable protocol takes
        assertEquals(
            """{"id":{"@type":"type.googleapis.com/google.protobuf.Int64Value","value":"9007199254740993"}}""",
            JSON.stringify(functions.convertToPlainValue(mapOf("id" to 9007199254740993L))),
        )
    }

    @Test
    fun rejectsOtherJsObjects() {
        assertFailsWith<IllegalArgumentException> { functions.convertToPlainValue(jsDate()) }
    }
}
