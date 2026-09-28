/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

import com.google.firebase.dataconnect.AnyValue
import com.google.firebase.dataconnect.DataConnectException
import com.google.firebase.dataconnect.fromAny
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/*
 * The JSON side of the Data Connect wire format: variables are encoded to a JSON object, data decoded from one, with
 * the SDK's string-based serializers (Date, Timestamp, UUID, enums) working unchanged.
 *
 * Variables go through the JSON tree encoder rather than the streaming one so that OptionalVariable.Undefined, whose
 * serializer encodes nothing, leaves its field out instead of a dangling key. Defaults are encoded, as the Android SDK's
 * encoder does. Data is decoded leniently: the server sends Int64 scalars as strings, and fields this code does not know
 * are ignored.
 */

private val encodingJson = Json {
    encodeDefaults = true
    explicitNulls = true
}

private val decodingJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

private fun Json.with(serializersModule: SerializersModule?): Json = if (serializersModule == null) this else Json(this) { this.serializersModule = serializersModule }

internal fun encodingJson(serializersModule: SerializersModule?): Json = encodingJson.with(serializersModule)

internal fun decodingJson(serializersModule: SerializersModule?): Json = decodingJson.with(serializersModule)

internal fun <Variables> encodeVariables(variables: Variables, serializer: SerializationStrategy<Variables>, serializersModule: SerializersModule?): JsonObject {
    val element = encodingJson(serializersModule).encodeToJsonElement(serializer, variables)
    return element as? JsonObject
        ?: throw DataConnectException("variables must serialize to a JSON object, but ${serializer.descriptor.serialName} serialized to ${element::class.simpleName}")
}

internal fun <Data> decodeData(data: JsonElement, deserializer: DeserializationStrategy<Data>, serializersModule: SerializersModule?): Data = decodingJson(serializersModule).decodeFromJsonElement(deserializer, data)

/** The plain Kotlin value of a JSON element: `null`, String, Boolean, Double, List or Map. */
internal fun JsonElement.toNaturalValue(): Any? = when (this) {
    is JsonNull -> null
    is JsonPrimitive -> if (isString) content else booleanOrNull ?: doubleOrNull ?: content
    is JsonArray -> map { it.toNaturalValue() }
    is JsonObject -> mapValues { it.value.toNaturalValue() }
}

/** The JSON element of a plain Kotlin value, or of an [AnyValue]. */
internal fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is AnyValue -> value.toJsonElement()
    is String -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is List<*> -> JsonArray(map { it.toJsonElement() })
    is Map<*, *> -> JsonObject(entries.associate { (key, value) -> key.toString() to value.toJsonElement() })
    else -> throw SerializationException("unsupported type: ${this::class.simpleName} (supported types: null, String, Boolean, Double, List<Any?>, Map<String, Any?>)")
}

internal actual fun <T> encodeToAnyValue(value: T, serializer: SerializationStrategy<T>, serializersModule: SerializersModule?): AnyValue {
    val natural = encodingJson(serializersModule).encodeToJsonElement(serializer, value).toNaturalValue()
        ?: throw IllegalArgumentException("NULL_VALUE is not allowed; just use null")
    return AnyValue.fromAny(natural)
}

internal actual fun <T> decodeFromAnyValue(anyValue: AnyValue, deserializer: DeserializationStrategy<T>, serializersModule: SerializersModule?): T = decodingJson(serializersModule).decodeFromJsonElement(deserializer, anyValue.value.toJsonElement())

internal actual fun Encoder.encodeAnyValue(value: AnyValue) {
    if (this is JsonEncoder) encodeJsonElement(value.value.toJsonElement()) else encodeSerializableValue(NaturalValueSerializer, value.value)
}

internal actual fun Decoder.decodeAnyValue(): AnyValue {
    val decoder = this as? JsonDecoder ?: throw SerializationException("AnyValue can only be decoded from JSON, not with ${this::class.simpleName}")
    val natural = decoder.decodeJsonElement().toNaturalValue() ?: throw SerializationException("AnyValue cannot hold null; use a nullable AnyValue? field")
    return AnyValue.fromAny(natural)
}

/** Encodes a plain value structurally, for encoders that are not JSON; decoding is not supported. */
@OptIn(InternalSerializationApi::class)
private object NaturalValueSerializer : KSerializer<Any?> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("com.google.firebase.dataconnect.AnyValue", SerialKind.CONTEXTUAL)

    override fun serialize(encoder: Encoder, value: Any?) {
        when (value) {
            null -> encoder.encodeNull()
            is AnyValue -> serialize(encoder, value.value)
            is String -> encoder.encodeString(value)
            is Boolean -> encoder.encodeBoolean(value)
            is Number -> encoder.encodeDouble(value.toDouble())
            is List<*> -> encoder.encodeSerializableValue(ListSerializer(this), value)
            is Map<*, *> -> encoder.encodeSerializableValue(MapSerializer(String.serializer(), this), value.entries.associate { it.key.toString() to it.value })
            else -> throw SerializationException("unsupported type: ${value::class.simpleName}")
        }
    }

    override fun deserialize(decoder: Decoder): Any? = throw SerializationException("AnyValue can only be decoded from JSON")
}
