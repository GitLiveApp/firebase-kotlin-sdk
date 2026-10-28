/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import dev.gitlive.firebase.externals.jsonStringify
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Serializable
data class JsReply(val name: String, val count: Int, val tags: List<String>, val nested: JsNested)

@Serializable
data class JsNested(val flag: Boolean)

private fun reply(): JsAny = js("""({name: "a", count: 2, tags: ["x", "y"], nested: {flag: true, missing: undefined, nothing: null}})""")

private fun jsString(): JsAny = js("'a'")

private fun jsArray(): JsAny = js("[1, 2]")

private fun jsDate(): JsAny = js("new Date(0)")

/** The values the JS SDK returns, as JSON.parse produces them, are read as plain values; plain values are written as JS values. */
class PlatformValuesTest {

    @Test
    fun readsTheJsSdksValuesAsPlainValues() {
        val expected = mapOf("name" to "a", "count" to 2, "tags" to listOf("x", "y"), "nested" to mapOf("flag" to true, "missing" to null, "nothing" to null))
        assertPlain(expected, functions.convertToPlainValue(reply()))
        assertPlain(expected, functions.decodeFromPlainValue<Map<String, Any?>>(reply()))
    }

    @Test
    fun decodesTheJsSdksValues() {
        assertEquals(JsReply("a", 2, listOf("x", "y"), JsNested(true)), functions.decodeFromPlainValue<JsReply>(reply()))
        assertEquals("a", functions.decodeFromPlainValue<String>(jsString()))
        assertEquals(listOf(1, 2), functions.decodeFromPlainValue<List<Int>>(jsArray()))
    }

    @Test
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    fun writesPlainValuesAsTheJsSdksValues() {
        val platform = functions.convertToPlatformValue(mapOf("a" to listOf(1L, 2.5), "b" to mapOf("c" to "d"), "e" to null))
        assertEquals("""{"a":[1,2.5],"b":{"c":"d"},"e":null}""", jsonStringify(platform as JsAny?))
    }

    @Test
    fun rejectsOtherJsObjects() {
        assertFailsWith<IllegalArgumentException> { functions.convertToPlainValue(jsDate()) }
    }
}
