/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect

import com.google.firebase.dataconnect.AnyValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The module's AnyValueSerializer reads and writes the JSON an `Any` scalar holds; the Android SDK's is a placeholder
 * that only its own protobuf encoder understands, so this is not run on Android.
 */
class AnyValueJsonTest {

    @Serializable
    data class Row(val extra: AnyValue?, val other: AnyValue)

    @Test
    fun anyValueSerializerRoundTripsJson() {
        val row = Row(extra = AnyValue(mapOf("n" to 1.5, "list" to listOf("a", null, false), "nested" to mapOf("k" to "v"))), other = AnyValue("fixed"))
        val json = Json.encodeToString(Row.serializer(), row)
        assertEquals("""{"extra":{"n":1.5,"list":["a",null,false],"nested":{"k":"v"}},"other":"fixed"}""", json)
        assertEquals(row, Json.decodeFromString(Row.serializer(), json))
        assertEquals(Row(extra = null, other = AnyValue("fixed")), Json.decodeFromString(Row.serializer(), """{"extra":null,"other":"fixed"}"""))
    }
}
