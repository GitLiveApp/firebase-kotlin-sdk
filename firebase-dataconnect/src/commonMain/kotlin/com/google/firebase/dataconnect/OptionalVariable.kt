/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A variable of an operation that may be left undefined, which is distinct from `null`: an [Undefined] variable is not
 * sent to the server at all (and takes the operation's default), a [Value] is sent even when it holds `null`.
 */
@Serializable(with = OptionalVariable.Serializer::class)
public sealed interface OptionalVariable<out T> {

    /** The value, or `null` when undefined. */
    public fun valueOrNull(): T?

    /** The value; throws when undefined. */
    public fun valueOrThrow(): T

    /** The undefined variable: omitted when the variables are serialized. */
    public object Undefined : OptionalVariable<Nothing> {
        override fun valueOrNull(): Nothing? = null

        override fun valueOrThrow(): Nothing = throw UndefinedValueException()

        override fun toString(): String = "undefined"

        private class UndefinedValueException : IllegalStateException("Undefined does not have a value")
    }

    /** A defined variable, possibly holding `null`. */
    public class Value<T>(public val value: T) : OptionalVariable<T> {
        override fun valueOrNull(): T = value

        override fun valueOrThrow(): T = value

        override fun equals(other: Any?): Boolean = other is Value<*> && value == other.value

        override fun hashCode(): Int = value?.hashCode() ?: 0

        override fun toString(): String = value?.toString() ?: "null"
    }

    /**
     * Serializes a [Value] with [elementSerializer] and an [Undefined] as nothing at all, so that the field is absent from
     * the encoded variables. Only encoders that tolerate an element encoding nothing support this (the tree encoder
     * of kotlinx-serialization-json does, its streaming encoder does not). Decoding is not supported, as variables are
     * only ever sent.
     */
    public class Serializer<T>(private val elementSerializer: KSerializer<T>) : KSerializer<OptionalVariable<T>> {
        override val descriptor: SerialDescriptor = elementSerializer.descriptor

        override fun deserialize(decoder: Decoder): OptionalVariable<T> = throw UnsupportedOperationException("OptionalVariableSerializer does not support decoding")

        override fun serialize(encoder: Encoder, value: OptionalVariable<T>) {
            when (value) {
                is Undefined -> {
                    // nothing to do: the field is omitted
                }
                is Value<T> -> encoder.encodeSerializableValue(elementSerializer, value.value)
            }
        }
    }
}
