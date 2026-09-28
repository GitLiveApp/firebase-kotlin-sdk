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

// Never called on Android: the callers are header stubs of the Android SDK's AnyValueKt and AnyValueSerializer.

internal actual fun <T> encodeToAnyValue(value: T, serializer: SerializationStrategy<T>, serializersModule: SerializersModule?): AnyValue = stub()

internal actual fun <T> decodeFromAnyValue(anyValue: AnyValue, deserializer: DeserializationStrategy<T>, serializersModule: SerializersModule?): T = stub()

internal actual fun Encoder.encodeAnyValue(value: AnyValue): Unit = stub()

internal actual fun Decoder.decodeAnyValue(): AnyValue = stub()
