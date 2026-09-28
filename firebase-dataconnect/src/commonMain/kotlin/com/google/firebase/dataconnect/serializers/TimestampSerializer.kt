/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect.serializers

import com.google.firebase.Timestamp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * Serializes a [Timestamp] as the RFC 3339 string of the Data Connect `Timestamp` scalar: always UTC with nine
 * fractional digits when encoding (`2024-01-02T03:04:05.123456789Z`), any offset and up to nine fractional digits when
 * decoding. Written over `kotlin.time.Instant` (the Android SDK's uses `java.text.SimpleDateFormat`), specific to the
 * Data Connect wire format.
 */
public object TimestampSerializer : KSerializer<Timestamp> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Timestamp", PrimitiveKind.STRING)

    private val pattern = Regex("""^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{0,9})?(Z|[+-]\d{2}:\d{2})$""")

    override fun serialize(encoder: Encoder, value: Timestamp) {
        val seconds = Instant.fromEpochSeconds(value.seconds, 0).toString().removeSuffix("Z")
        encoder.encodeString(seconds + "." + value.nanoseconds.toString().padStart(9, '0') + "Z")
    }

    override fun deserialize(decoder: Decoder): Timestamp {
        val string = decoder.decodeString()
        val normalized = string.uppercase()
        require(normalized.matches(pattern)) {
            "Value does not conform to the RFC3339 specification with up to 9 digits of time-secfrac precision (str=$string)."
        }
        // A fraction with no digits ("12:00:00.Z") is allowed by the Android SDK and not by Instant.parse.
        val instant = Instant.parse(normalized.replace(".Z", "Z").replace(Regex("""\.([+-])"""), "$1"))
        return Timestamp(instant.epochSeconds, instant.nanosecondsOfSecond)
    }
}
