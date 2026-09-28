/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect.serializers

import com.google.firebase.dataconnect.EnumValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes an [EnumValue] as the enum constant's name, decoding a name that is not among [values] as
 * [EnumValue.Unknown] rather than failing. Pure Kotlin, not tied to any wire format.
 */
public open class EnumValueSerializer<T : Enum<T>>(values: Iterable<T>) : KSerializer<EnumValue<T>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.google.firebase.dataconnect.EnumValue", PrimitiveKind.STRING)

    private val enumValueByStringValue: Map<String, T> = buildMap {
        for (value in values) {
            val oldValue = put(value.name, value)
            require(oldValue === null) { "duplicate value.name in values: ${value.name}" }
        }
    }

    override fun deserialize(decoder: Decoder): EnumValue<T> {
        val stringValue = decoder.decodeString()
        val enumValue = enumValueByStringValue[stringValue] ?: return EnumValue.Unknown(stringValue)
        return EnumValue.Known(enumValue)
    }

    override fun serialize(encoder: Encoder, value: EnumValue<T>) {
        encoder.encodeString(value.stringValue)
    }
}
