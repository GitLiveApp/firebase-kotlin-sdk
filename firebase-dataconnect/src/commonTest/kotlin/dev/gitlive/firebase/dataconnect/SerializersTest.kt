/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect

import com.google.firebase.Timestamp
import com.google.firebase.dataconnect.AnyValue
import com.google.firebase.dataconnect.EnumValue
import com.google.firebase.dataconnect.LocalDate
import com.google.firebase.dataconnect.OptionalVariable
import com.google.firebase.dataconnect.copy
import com.google.firebase.dataconnect.decode
import com.google.firebase.dataconnect.encode
import com.google.firebase.dataconnect.fromAny
import com.google.firebase.dataconnect.serializers.EnumValueSerializer
import com.google.firebase.dataconnect.serializers.KotlinxDatetimeLocalDateSerializer
import com.google.firebase.dataconnect.serializers.LocalDateSerializer
import com.google.firebase.dataconnect.serializers.TimestampSerializer
import com.google.firebase.dataconnect.toDataConnectLocalDate
import com.google.firebase.dataconnect.toKotlinxLocalDate
import dev.gitlive.firebase.dataconnect.serializers.UuidSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

/**
 * Round trips of every serializer of the `serializers` package (and the dev.gitlive `UuidSerializer`) through JSON, the
 * Data Connect wire format. Runs on every target: on Android against the Android SDK's serializers, elsewhere against
 * this module's.
 */
class SerializersTest {

    enum class Status { ACTIVE, INACTIVE }

    object StatusSerializer : EnumValueSerializer<Status>(Status.entries)

    @Serializable
    data class Variables(
        val undefined: OptionalVariable<Int?> = OptionalVariable.Undefined,
        val nullValue: OptionalVariable<Int?> = OptionalVariable.Value(null),
        val value: OptionalVariable<Int?> = OptionalVariable.Value(3),
        val nested: Nested = Nested(),
        val trailing: String = "end",
    )

    @Serializable
    data class Nested(val undefined: OptionalVariable<String> = OptionalVariable.Undefined, val date: OptionalVariable<LocalDate> = OptionalVariable.Value(LocalDate(2024, 2, 29)))

    @Serializable
    data class Extra(val label: String, val count: Double, val flags: List<Boolean>, val nothing: String?)

    @Test
    fun localDateSerializer() {
        assertEquals("\"2024-01-02\"", Json.encodeToString(LocalDateSerializer, LocalDate(2024, 1, 2)))
        assertEquals("\"0099-12-31\"", Json.encodeToString(LocalDateSerializer, LocalDate(99, 12, 31)))
        assertEquals(LocalDate(2024, 1, 2), Json.decodeFromString(LocalDateSerializer, "\"2024-01-02\""))
        assertEquals(LocalDate(2024, 1, 2), Json.decodeFromString(LocalDateSerializer, "\"2024-1-2\""))
        assertFailsWith<IllegalArgumentException> { Json.decodeFromString(LocalDateSerializer, "\"2024/01/02\"") }
        assertEquals(LocalDate(2024, 3, 2), LocalDate(2024, 1, 2).copy(month = 3))
        assertEquals(kotlinx.datetime.LocalDate(2024, 1, 2), LocalDate(2024, 1, 2).toKotlinxLocalDate())
        assertEquals(LocalDate(2024, 1, 2), kotlinx.datetime.LocalDate(2024, 1, 2).toDataConnectLocalDate())
        assertEquals(LocalDate(2024, 1, 2), LocalDate(2024, 1, 2))
        assertNotEquals(LocalDate(2024, 1, 2), LocalDate(2024, 1, 3))
    }

    @Test
    fun kotlinxDatetimeLocalDateSerializer() {
        val date = kotlinx.datetime.LocalDate(2024, 1, 2)
        assertEquals("\"2024-01-02\"", Json.encodeToString(KotlinxDatetimeLocalDateSerializer, date))
        assertEquals(date, Json.decodeFromString(KotlinxDatetimeLocalDateSerializer, "\"2024-01-02\""))
    }

    @Test
    fun timestampSerializer() {
        val timestamp = Timestamp(1700000000, 123456789)
        assertEquals("\"2023-11-14T22:13:20.123456789Z\"", Json.encodeToString(TimestampSerializer, timestamp))
        assertEquals("\"2023-11-14T22:13:20.000000000Z\"", Json.encodeToString(TimestampSerializer, Timestamp(1700000000, 0)))
        assertEquals(timestamp, Json.decodeFromString(TimestampSerializer, "\"2023-11-14T22:13:20.123456789Z\""))
        assertEquals(Timestamp(1700000000, 123000000), Json.decodeFromString(TimestampSerializer, "\"2023-11-14T22:13:20.123Z\""))
        assertEquals(Timestamp(1700000000, 0), Json.decodeFromString(TimestampSerializer, "\"2023-11-14T22:13:20Z\""))
        assertEquals(Timestamp(1700000000, 0), Json.decodeFromString(TimestampSerializer, "\"2023-11-15T00:13:20+02:00\""))
        assertEquals(Timestamp(1700000000, 0), Json.decodeFromString(TimestampSerializer, "\"2023-11-14t22:13:20z\""))
        assertFailsWith<IllegalArgumentException> { Json.decodeFromString(TimestampSerializer, "\"2023-11-14 22:13:20\"") }
    }

    @Test
    fun enumValueSerializer() {
        assertEquals("\"ACTIVE\"", Json.encodeToString(StatusSerializer, EnumValue.Known(Status.ACTIVE)))
        assertEquals("\"RETIRED\"", Json.encodeToString(StatusSerializer, EnumValue.Unknown("RETIRED")))
        assertEquals(EnumValue.Known(Status.INACTIVE), Json.decodeFromString(StatusSerializer, "\"INACTIVE\""))
        val unknown = Json.decodeFromString(StatusSerializer, "\"RETIRED\"")
        assertEquals(EnumValue.Unknown("RETIRED"), unknown)
        assertNull(unknown.value)
        assertEquals("RETIRED", unknown.stringValue)
        assertEquals(EnumValue.Known(Status.ACTIVE), EnumValue.Known(Status.INACTIVE).copy(value = Status.ACTIVE))
    }

    @Test
    fun optionalVariableSerializerOmitsUndefined() {
        val encoded = Json { encodeDefaults = true }.encodeToJsonElement(Variables.serializer(), Variables()).jsonObject
        assertEquals(setOf("nullValue", "value", "nested", "trailing"), encoded.keys)
        assertEquals(JsonNull, encoded["nullValue"])
        assertEquals(JsonPrimitive(3), encoded["value"])
        assertEquals(JsonPrimitive("end"), encoded["trailing"])
        assertEquals(setOf("date"), encoded["nested"]!!.jsonObject.keys)
        assertEquals(JsonPrimitive("2024-02-29"), encoded["nested"]!!.jsonObject["date"])
        assertEquals(3, OptionalVariable.Value(3).valueOrThrow())
        assertNull(OptionalVariable.Undefined.valueOrNull())
        assertFailsWith<IllegalStateException> { OptionalVariable.Undefined.valueOrThrow() }
        assertFailsWith<UnsupportedOperationException> { Json.decodeFromString(Variables.serializer(), """{"value":3}""") }
    }

    @Test
    fun uuidSerializer() {
        val uuid = Uuid.parse("123e4567-e89b-12d3-a456-426614174000")
        assertEquals("\"123e4567e89b12d3a456426614174000\"", Json.encodeToString(UuidSerializer, uuid))
        assertEquals(uuid, Json.decodeFromString(UuidSerializer, "\"123e4567e89b12d3a456426614174000\""))
        assertFailsWith<IllegalArgumentException> { Json.decodeFromString(UuidSerializer, "\"123e4567-e89b-12d3-a456-426614174000\"") }
    }

    @Test
    fun anyValue() {
        val map = AnyValue(mapOf("label" to "a", "count" to 2.0, "flags" to listOf(true, false), "nothing" to null))
        assertEquals(mapOf("label" to "a", "count" to 2.0, "flags" to listOf(true, false), "nothing" to null), map.value)
        assertEquals(map, AnyValue.fromAny(mapOf("label" to "a", "count" to 2.0, "flags" to listOf(true, false), "nothing" to null)))
        assertEquals("text", AnyValue("text").value)
        assertEquals(true, AnyValue(true).value)
        assertEquals(1.5, AnyValue(1.5).value)
        assertEquals(listOf("x", null), AnyValue(listOf("x", null)).value)
        assertNull(AnyValue.fromAny(null))
        assertFailsWith<IllegalArgumentException> { AnyValue.fromAny(Status.ACTIVE) }

        val extra = Extra(label = "a", count = 2.0, flags = listOf(true, false), nothing = null)
        val encoded = AnyValue.encode(extra)
        assertEquals(map, encoded)
        assertEquals(extra, encoded.decode<Extra>())
        assertEquals(extra, encoded.decode(Extra.serializer()))
        assertEquals(AnyValue("text"), AnyValue.encode("text"))
        assertContentEquals(listOf(1.0, 2.0), AnyValue.encode(listOf(1, 2)).decode<List<Double>>())
        assertIs<Map<*, *>>(AnyValue.encode(extra).value)
    }
}
