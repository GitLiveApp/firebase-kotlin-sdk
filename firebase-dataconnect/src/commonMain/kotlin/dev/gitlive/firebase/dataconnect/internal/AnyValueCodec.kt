/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

import com.google.firebase.dataconnect.AnyValue
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

/*
 * The conversions between an AnyValue and serializable values. On Android they are never called (the Android SDK's own
 * AnyValueKt and AnyValueSerializer are used at runtime, see the header stubs); everywhere else they go through
 * kotlinx-serialization-json, which stays out of the common API.
 */

internal expect fun <T> encodeToAnyValue(value: T, serializer: SerializationStrategy<T>, serializersModule: SerializersModule?): AnyValue

internal expect fun <T> decodeFromAnyValue(anyValue: AnyValue, deserializer: DeserializationStrategy<T>, serializersModule: SerializersModule?): T

internal expect fun Encoder.encodeAnyValue(value: AnyValue)

internal expect fun Decoder.decodeAnyValue(): AnyValue
