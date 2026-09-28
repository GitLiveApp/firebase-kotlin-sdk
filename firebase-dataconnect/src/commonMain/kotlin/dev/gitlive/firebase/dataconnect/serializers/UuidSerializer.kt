/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.uuid.Uuid

/**
 * Serializes a [kotlin.uuid.Uuid] as the 32 hexadecimal digits, without dashes, of the Data Connect `UUID` scalar. The
 * multiplatform counterpart of the Android SDK's `UUIDSerializer`, which serializes a `java.util.UUID` the same way
 * and is available on Android and the JVM only.
 */
public object UuidSerializer : KSerializer<Uuid> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("dev.gitlive.firebase.dataconnect.Uuid", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Uuid) {
        encoder.encodeString(value.toHexString())
    }

    override fun deserialize(decoder: Decoder): Uuid {
        val string = decoder.decodeString()
        require(string.length == 32) { "invalid UUID string: $string (expected 32 hexadecimal digits, got ${string.length} characters)" }
        return Uuid.parseHex(string)
    }
}
