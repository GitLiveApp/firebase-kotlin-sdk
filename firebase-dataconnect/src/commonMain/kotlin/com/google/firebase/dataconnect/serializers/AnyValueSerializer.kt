/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect.serializers

import com.google.firebase.dataconnect.AnyValue
import dev.gitlive.firebase.dataconnect.internal.decodeAnyValue
import dev.gitlive.firebase.dataconnect.internal.encodeAnyValue
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes an [AnyValue] as the JSON it holds. Encoding works with any encoder (the value is encoded structurally as
 * strings, booleans, doubles, lists and maps), decoding only with a JSON decoder, as the shape of the value is only
 * known once it is read. The Android SDK's is a placeholder that its own encoder recognises; on Android that one is used.
 */
@OptIn(InternalSerializationApi::class)
public object AnyValueSerializer : KSerializer<AnyValue> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("com.google.firebase.dataconnect.AnyValue", SerialKind.CONTEXTUAL)

    override fun serialize(encoder: Encoder, value: AnyValue) {
        encoder.encodeAnyValue(value)
    }

    override fun deserialize(decoder: Decoder): AnyValue = decoder.decodeAnyValue()
}
