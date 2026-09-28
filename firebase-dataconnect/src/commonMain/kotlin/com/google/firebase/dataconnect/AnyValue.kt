/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.dataconnect

import com.google.firebase.dataconnect.serializers.AnyValueSerializer
import dev.gitlive.firebase.dataconnect.internal.decodeFromAnyValue
import dev.gitlive.firebase.dataconnect.internal.encodeToAnyValue
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.jvm.JvmName

/**
 * The value of a Data Connect `Any` scalar: a [String], a [Boolean], a [Double], a [List] of such values (or `null`) or a
 * [Map] from [String] to such values (or `null`). A `null` scalar is represented by `null` rather than by an [AnyValue].
 *
 * The Android SDK keeps the value as a protobuf `Value`; this class keeps the plain Kotlin value, which is what [value]
 * returns on both. Numbers other than [Double] given to [fromAny] are converted to [Double], as the wire format has
 * only that number type.
 */
@Serializable(with = AnyValueSerializer::class)
public class AnyValue private constructor(public val value: Any, @Suppress("UNUSED_PARAMETER") natural: Unit) {
    public constructor(value: Map<String, Any?>) : this(value.toNaturalValue(), Unit)
    public constructor(value: List<Any?>) : this(value.toNaturalValue(), Unit)
    public constructor(value: String) : this(value, Unit)
    public constructor(value: Boolean) : this(value, Unit)
    public constructor(value: Double) : this(value, Unit)

    override fun equals(other: Any?): Boolean = other is AnyValue && other.value == value

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toNaturalString()

    public companion object
}

/** Decodes this value with [deserializer]. */
public fun <T> AnyValue.decode(deserializer: DeserializationStrategy<T>, serializersModule: SerializersModule? = null): T = decodeFromAnyValue(this, deserializer, serializersModule)

/** Decodes this value with the serializer of [T]. */
public inline fun <reified T> AnyValue.decode(): T = decode(serializer<T>())

/** Encodes [value] with [serializer]; a `null` result is an error, as [AnyValue] never holds `null`. */
public fun <T> AnyValue.Companion.encode(value: T, serializer: SerializationStrategy<T>, serializersModule: SerializersModule? = null): AnyValue = encodeToAnyValue(value, serializer, serializersModule)

/** Encodes [value] with the serializer of [T]. */
public inline fun <reified T> AnyValue.Companion.encode(value: T): AnyValue = encode(value, serializer<T>())

/** [fromAny] for a nullable value: `null` for `null`. */
@JvmName("fromNullableAny")
public fun AnyValue.Companion.fromAny(value: Any?): AnyValue? = if (value === null) null else fromAny(value)

/** Wraps a plain value; throws [IllegalArgumentException] for a type that an `Any` scalar cannot hold. */
public fun AnyValue.Companion.fromAny(value: Any): AnyValue {
    @Suppress("UNCHECKED_CAST")
    return when (value) {
        is String -> AnyValue(value)
        is Boolean -> AnyValue(value)
        is Double -> AnyValue(value)
        is Number -> AnyValue(value.toDouble())
        is List<*> -> AnyValue(value)
        is Map<*, *> -> AnyValue(value as Map<String, Any?>)
        else -> throw IllegalArgumentException(
            "unsupported type: ${value::class.simpleName}" +
                " (supported types: null, String, Boolean, Double, List<Any?>, Map<String, Any?>)",
        )
    }
}

/** A validated, immutable copy of a plain value: the supported types only, numbers as [Double]. */
private fun Any?.toNaturalValue(): Any? = when (this) {
    null -> null
    is String, is Boolean, is Double -> this
    is Number -> toDouble()
    is AnyValue -> value
    is List<*> -> map { it.toNaturalValue() }
    is Map<*, *> -> entries.associate { (key, value) ->
        require(key is String) { "unsupported map key type: ${key?.let { it::class.simpleName }} (map keys must be String)" }
        key to value.toNaturalValue()
    }
    else -> throw IllegalArgumentException(
        "unsupported type: ${this::class.simpleName}" +
            " (supported types: null, String, Boolean, Double, List<Any?>, Map<String, Any?>)",
    )
}

private fun Map<String, Any?>.toNaturalValue(): Map<String, Any?> = (this as Any).toNaturalValue() as Map<String, Any?>

private fun List<Any?>.toNaturalValue(): List<Any?> = (this as Any).toNaturalValue() as List<Any?>

/** A compact JSON-like rendering with sorted keys, like the Android SDK's. */
private fun Any?.toNaturalString(): String = when (this) {
    null -> "null"
    is String -> "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    is List<*> -> joinToString(",", "[", "]") { it.toNaturalString() }
    is Map<*, *> -> entries.sortedBy { it.key.toString() }.joinToString(",", "{", "}") { "\"${it.key}\":${it.value.toNaturalString()}" }
    else -> toString()
}
