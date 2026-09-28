/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect.serializers

import com.google.firebase.dataconnect.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes a [LocalDate] as the `YYYY-MM-DD` string of the Data Connect `Date` scalar, accepting any `-`-separated
 * three integers when decoding. Pure Kotlin (the Android SDK's uses a `java.util.regex` pattern), specific to the Data
 * Connect wire format.
 */
public object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.google.firebase.dataconnect.LocalDate", PrimitiveKind.STRING)

    private val pattern = Regex("""^(-?\d+)-(-?\d+)-(-?\d+)$""")

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.year.pad(4) + "-" + value.month.pad(2) + "-" + value.day.pad(2))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        val string = decoder.decodeString()
        val match = pattern.matchEntire(string) ?: throw IllegalArgumentException("date string does not match the pattern YYYY-MM-DD: $string")
        val (year, month, day) = match.destructured
        return LocalDate(year = year.toIntOrThrow("year", string), month = month.toIntOrThrow("month", string), day = day.toIntOrThrow("day", string))
    }

    private fun Int.pad(digits: Int): String = if (this < 0) "-" + (-this).toString().padStart(digits, '0') else toString().padStart(digits, '0')

    private fun String.toIntOrThrow(part: String, string: String): Int = toIntOrNull() ?: throw IllegalArgumentException("invalid $part in date string: $string")
}
