/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalSerializationApi::class)

package com.google.firebase.serialization

import com.google.firebase.Timestamp
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.capturedKClass
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlin.time.Instant

/** The serial names of the collections the Android SDK rejects: sets (any `Collection` but a `List`) and arrays. */
private val setNames = setOf("kotlin.collections.LinkedHashSet", "kotlin.collections.HashSet")
private val arrayNames = setOf(
    "kotlin.Array", "kotlin.IntArray", "kotlin.LongArray", "kotlin.DoubleArray", "kotlin.FloatArray", "kotlin.BooleanArray",
    "kotlin.ByteArray", "kotlin.ShortArray", "kotlin.CharArray", "kotlin.UIntArray", "kotlin.ULongArray", "kotlin.UByteArray",
    "kotlin.UShortArray",
)

internal fun SerialDescriptor.baseName() = serialName.removeSuffix("?")

internal fun SerialDescriptor.isSet() = kind == StructureKind.LIST && baseName() in setNames

internal fun SerialDescriptor.isArray() = kind == StructureKind.LIST && baseName() in arrayNames

/**
 * Converts a value to plain values by its runtime type: the counterpart of the Android SDK's
 * `CustomClassMapper.serialize(Object)` and Cloud Functions' `Serializer.encode(Object)`.
 */
internal fun encodeRuntimeValue(format: FirebaseFormat, value: Any?, path: ErrorPath): Any? {
    val rules = format.rules
    rules.checkDepth(path)
    return when {
        value == null -> null
        value is Number -> rules.encodeNumber(value, path)
        value is String || value is Boolean -> value
        value is Char -> rules.rejectChar(value, path)
        value is Map<*, *> -> HashMap<String, Any?>().also { result ->
            for ((key, element) in value) {
                if (key !is String) rules.rejectMapKey(path)
                result[key] = encodeRuntimeValue(format, element, path.child(key))
            }
        }
        value is List<*> -> ArrayList<Any?>(value.size).also { result ->
            value.forEachIndexed { index, element -> result.add(encodeRuntimeValue(format, element, path.child("[$index]"))) }
        }
        value is Collection<*> -> rules.rejectCollection(value, path)
        isArray(value) -> rules.rejectArray(value, path)
        rules.isNativeValue(value) -> value
        !rules.mapsObjects -> rules.rejectValue(value, path)
        value is Instant && rules is Rules.Firestore -> Timestamp(value.epochSeconds, value.nanosecondsOfSecond)
        else -> {
            val serializer = format.serializerOf(value::class)
            when {
                serializer != null -> PlainEncoder(format, path).apply { encodeSerializableValue(serializer, value) }.value
                value is Enum<*> -> value.name
                else -> rules.rejectValue(value, path)
            }
        }
    }
}

private fun Rules.checkDepth(path: ErrorPath) {
    val max = maxDepth ?: return
    if (path.length > max) throw serializeError(path, "Exceeded maximum depth of $max, which likely indicates there's an object cycle")
}

/** Encodes one value to plain values; [sink] receives the value, and again if it is replaced. */
internal class PlainEncoder(
    private val format: FirebaseFormat,
    private val path: ErrorPath,
    private val sink: (Any?) -> Unit = {},
) : Encoder {
    private val rules = format.rules

    var value: Any? = null
        set(value) {
            field = value
            sink(value)
        }

    init {
        rules.checkDepth(path)
    }

    override val serializersModule: SerializersModule get() = format.serializersModule

    override fun encodeNull() {
        value = null
    }

    override fun encodeBoolean(value: Boolean) {
        this.value = value
    }

    override fun encodeByte(value: Byte) {
        this.value = rules.encodeSmallNumber(value, "Byte", path)
    }

    override fun encodeShort(value: Short) {
        this.value = rules.encodeSmallNumber(value, "Short", path)
    }

    override fun encodeInt(value: Int) {
        this.value = rules.encodeNumber(value, path)
    }

    override fun encodeLong(value: Long) {
        this.value = rules.encodeNumber(value, path)
    }

    override fun encodeFloat(value: Float) {
        this.value = rules.encodeNumber(value, path)
    }

    override fun encodeDouble(value: Double) {
        this.value = rules.encodeNumber(value, path)
    }

    override fun encodeChar(value: Char) {
        rules.rejectChar(value, path)
    }

    override fun encodeString(value: String) {
        this.value = value
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        value = enumDescriptor.getElementAnnotations(index).firstNotNullOfOrNull(rules.enumPropertyName)
            ?: enumDescriptor.getElementName(index)
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = when (descriptor.kind) {
        StructureKind.LIST -> when {
            descriptor.isArray() -> rules.rejectArray(null, path)
            descriptor.isSet() -> rules.rejectCollection(null, path)
            else -> ListEncoder(format, path).also { value = it.result }
        }
        StructureKind.MAP -> MapEncoder(format, path, descriptor.getElementDescriptor(0)).also { value = it.result }
        StructureKind.CLASS, StructureKind.OBJECT -> ClassEncoder(format, path, ClassMapping(format, descriptor)).also { value = it.result }
        is PolymorphicKind -> PolymorphicEncoder(format, path) { value = it }
        else -> throw rules.serializeError(path, "Values of kind ${descriptor.kind} (${descriptor.serialName}) are not supported")
    }

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        val descriptor = serializer.descriptor
        when {
            value == null -> serializer.serialize(this, value)
            descriptor.kind == SerialKind.CONTEXTUAL -> when {
                rules.isNativeValue(value) -> this.value = value
                descriptor.capturedKClass == Any::class -> this.value = encodeRuntimeValue(format, value, path)
                else -> serializer.serialize(this, value)
            }
            // Checked here, where the value is known, for the messages that print it
            descriptor.isArray() -> rules.rejectArray(value, path)
            descriptor.isSet() -> rules.rejectCollection(value, path)
            value is Instant && rules is Rules.Firestore -> this.value = Timestamp(value.epochSeconds, value.nanosecondsOfSecond)
            else -> serializer.serialize(this, value)
        }
    }
}

/** An encoder for the elements of a structure, each encoded by a [PlainEncoder] for its path. */
internal abstract class ElementEncoder(protected val format: FirebaseFormat) : CompositeEncoder {
    protected val rules = format.rules

    override val serializersModule: SerializersModule get() = format.serializersModule

    /** The encoder for element [index], or `null` to skip it. */
    protected abstract fun elementEncoder(descriptor: SerialDescriptor, index: Int): PlainEncoder?

    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int): Boolean = true

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        elementEncoder(descriptor, index)?.encodeBoolean(value)
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        elementEncoder(descriptor, index)?.encodeByte(value)
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        elementEncoder(descriptor, index)?.encodeShort(value)
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        elementEncoder(descriptor, index)?.encodeInt(value)
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        elementEncoder(descriptor, index)?.encodeLong(value)
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        elementEncoder(descriptor, index)?.encodeFloat(value)
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        elementEncoder(descriptor, index)?.encodeDouble(value)
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        elementEncoder(descriptor, index)?.encodeChar(value)
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        elementEncoder(descriptor, index)?.encodeString(value)
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder = elementEncoder(descriptor, index) ?: PlainEncoder(format, ErrorPath.EMPTY)

    override fun <T> encodeSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T) {
        elementEncoder(descriptor, index)?.encodeSerializableValue(serializer, value)
    }

    override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) {
        elementEncoder(descriptor, index)?.encodeNullableSerializableValue(serializer, value)
    }

    override fun endStructure(descriptor: SerialDescriptor) {}
}

/** A class, written as a map of its properties: the counterpart of the Android SDK's `BeanMapper.serialize`. */
internal class ClassEncoder(format: FirebaseFormat, private val path: ErrorPath, private val mapping: ClassMapping) : ElementEncoder(format) {
    val result = HashMap<String, Any?>()

    override fun elementEncoder(descriptor: SerialDescriptor, index: Int): PlainEncoder? {
        if (mapping.excluded[index] || mapping.documentId[index]) return null
        val name = mapping.names[index]
        return PlainEncoder(format, path.child(name)) { result[name] = it }
    }

    override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) {
        if (value == null && mapping.serverTimestamp[index] && !mapping.excluded[index] && rules is Rules.Firestore) {
            result[mapping.names[index]] = rules.bindings.serverTimestamp()
        } else {
            super.encodeNullableSerializableElement(descriptor, index, serializer, value)
        }
    }
}

internal class ListEncoder(format: FirebaseFormat, private val path: ErrorPath) : ElementEncoder(format) {
    val result = ArrayList<Any?>()

    override fun elementEncoder(descriptor: SerialDescriptor, index: Int): PlainEncoder {
        while (result.size <= index) result.add(null)
        return PlainEncoder(format, path.child("[$index]")) { result[index] = it }
    }
}

/** A map, whose keys must be strings: the elements alternate between keys and values. */
internal class MapEncoder(format: FirebaseFormat, private val path: ErrorPath, keyDescriptor: SerialDescriptor) : ElementEncoder(format) {
    val result = HashMap<String, Any?>()
    private var key: String? = null

    init {
        if (keyDescriptor.kind != PrimitiveKind.STRING && keyDescriptor.kind != SerialKind.CONTEXTUAL) rules.rejectMapKey(path)
    }

    override fun elementEncoder(descriptor: SerialDescriptor, index: Int): PlainEncoder = if (index % 2 == 0) {
        PlainEncoder(format, path) { key = it as? String ?: rules.rejectMapKey(path) }
    } else {
        val key = key!!
        PlainEncoder(format, path.child(key)) { result[key] = it }
    }
}

/**
 * A sealed or open polymorphic value: kotlinx.serialization writes the subclass's serial name and then the value, which
 * is stored as the value's map with the serial name as its `type` property.
 */
internal class PolymorphicEncoder(format: FirebaseFormat, private val path: ErrorPath, private val sink: (Any?) -> Unit) : ElementEncoder(format) {
    private var type: String? = null
    private var value: Any? = null

    override fun elementEncoder(descriptor: SerialDescriptor, index: Int): PlainEncoder = when (index) {
        0 -> PlainEncoder(format, path) { type = it as String }
        else -> PlainEncoder(format, path) { value = it }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        @Suppress("UNCHECKED_CAST")
        val map = value as? MutableMap<String, Any?>
            ?: throw rules.serializeError(path, "Polymorphic values must be classes, but ${type ?: descriptor.serialName} is not")
        map[POLYMORPHIC_TYPE] = type
        sink(map)
    }
}

/** The property that holds the serial name of a polymorphic value's class. */
internal const val POLYMORPHIC_TYPE = "type"
