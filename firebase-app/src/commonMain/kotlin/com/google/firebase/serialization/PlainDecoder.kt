/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalSerializationApi::class)

package com.google.firebase.serialization

import com.google.firebase.Timestamp
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.capturedKClass
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.CompositeDecoder.Companion.UNKNOWN_NAME
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import kotlin.time.Instant

/**
 * Decodes plain values: the counterpart of the Android SDK's `CustomClassMapper.deserializeToType`.
 *
 * @param nullable whether the target is nullable, which the Android SDK maps to a boxed Java type, for messages.
 * @param absent whether there is no value at all (a property missing from the data), which decodes as `null`, or as
 *   zero for a primitive, as a field the Android SDK does not set keeps the zero value Java gives it.
 * @param ignoredKey a property of the data that is not a class property, the type of a polymorphic value.
 */
internal class PlainDecoder(
    private val format: FirebaseFormat,
    private val value: Any?,
    private val path: ErrorPath,
    private val documentReference: Any?,
    private val nullable: Boolean = false,
    private val absent: Boolean = false,
    private val ignoredKey: String? = null,
) : Decoder {
    private val rules = format.rules

    override val serializersModule: SerializersModule get() = format.serializersModule

    private fun fail(reason: String): Nothing = throw rules.deserializeError(path, reason)

    /** The Java type name of a target, for messages. */
    private fun primitive(name: String, boxed: String) = if (nullable) boxed else name

    private fun typeOfValue(): String = value?.let(::javaClassName) ?: "null"

    override fun decodeNotNullMark(): Boolean = value != null

    override fun decodeNull(): Nothing? = null

    override fun decodeBoolean(): Boolean = when {
        absent -> false
        value is Boolean -> value
        else -> fail("Failed to convert value of type ${typeOfValue()} to boolean")
    }

    // Integer, Long and Double values, as the Android SDK accepts. Converted through toDouble(), which is exact for every
    // value in range, because on Kotlin/JS every number passes `is Int` (1.9 included).
    override fun decodeInt(): Int {
        if (absent) return 0
        return when (value) {
            is Int, is Long, is Double -> {
                val double = (value as Number).toDouble()
                if (double >= Int.MIN_VALUE.toDouble() && double <= Int.MAX_VALUE.toDouble()) {
                    double.toInt()
                } else {
                    fail("Numeric value out of 32-bit integer range: $double. Did you mean to use a long or double instead of an int?")
                }
            }
            else -> fail("Failed to convert a value of type ${typeOfValue()} to int")
        }
    }

    override fun decodeLong(): Long {
        if (absent) return 0
        return when (value) {
            is Long -> value
            is Int, is Double -> {
                val double = (value as Number).toDouble()
                if (double >= Long.MIN_VALUE.toDouble() && double <= Long.MAX_VALUE.toDouble()) {
                    double.toLong()
                } else {
                    fail("Numeric value out of 64-bit long range: $double. Did you mean to use a double instead of a long?")
                }
            }
            else -> fail("Failed to convert a value of type ${typeOfValue()} to long")
        }
    }

    override fun decodeDouble(): Double {
        if (absent) return 0.0
        return when (value) {
            is Int -> value.toDouble()
            is Long -> value.toDouble().also {
                if (it.toLong() != value) fail("Loss of precision while converting number to double: $value. Did you mean to use a 64-bit long instead?")
            }
            is Double -> value
            else -> fail("Failed to convert a value of type ${typeOfValue()} to double")
        }
    }

    override fun decodeFloat(): Float = decodeDouble().toFloat()

    override fun decodeShort(): Short = if (absent) 0 else fail("Deserializing values to ${primitive("short", "Short")} is not supported")

    override fun decodeByte(): Byte = if (absent) 0 else fail("Deserializing values to ${primitive("byte", "Byte")} is not supported")

    override fun decodeChar(): Char = if (absent) '\u0000' else fail("Deserializing values to ${primitive("char", "Character")} is not supported")

    override fun decodeString(): String = value as? String ?: fail("Failed to convert value of type ${typeOfValue()} to String")

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val name = enumDescriptor.baseName()
        val string = value as? String ?: fail("Expected a String while deserializing to enum class $name but got a ${value?.let(::javaClassString) ?: "null"}")
        for (index in 0 until enumDescriptor.elementsCount) {
            if (enumDescriptor.getElementAnnotations(index).firstNotNullOfOrNull(rules.enumPropertyName) == string) return index
        }
        val index = enumDescriptor.getElementIndex(string)
        if (index == UNKNOWN_NAME) fail("Could not find enum value of $name for value \"$string\"")
        return index
    }

    override fun decodeInline(descriptor: SerialDescriptor): Decoder = this

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val value = value ?: fail("Failed to convert null to ${descriptor.baseName()}")
        return when (descriptor.kind) {
            StructureKind.CLASS, StructureKind.OBJECT -> {
                val mapping = ClassMapping(format, descriptor)
                val map = value as? Map<*, *> ?: fail("Can't convert object of type ${javaClassName(value)} to type ${mapping.className}")
                ClassDecoder(format, map, path, documentReference, mapping, ignoredKey)
            }
            StructureKind.LIST -> when {
                descriptor.isArray() -> fail(rules.arrayTarget)
                descriptor.isSet() -> fail("Collections are not supported, please use Lists instead")
                else -> ListDecoder(format, value as? List<*> ?: fail(rules.expectedList(value)), path, documentReference)
            }
            StructureKind.MAP -> {
                val keyDescriptor = descriptor.getElementDescriptor(0)
                if (keyDescriptor.kind != PrimitiveKind.STRING && keyDescriptor.kind != SerialKind.CONTEXTUAL) {
                    fail("Only Maps with string keys are supported, but found Map with key type ${typeString(keyDescriptor, boxed = true)}")
                }
                MapDecoder(format, value as? Map<*, *> ?: fail("Expected a Map while deserializing, but got a ${javaClassString(value)}"), path, documentReference)
            }
            is PolymorphicKind -> PolymorphicDecoder(
                format,
                value as? Map<*, *> ?: fail("Expected a Map while deserializing, but got a ${javaClassString(value)}"),
                path,
                documentReference,
            )
            else -> fail("Values of kind ${descriptor.kind} (${descriptor.serialName}) are not supported")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        val descriptor = deserializer.descriptor
        if (value == null) return deserializer.deserialize(this)
        if (descriptor.kind == SerialKind.CONTEXTUAL) {
            val type = descriptor.capturedKClass
            return when {
                type == Any::class -> value as T
                type != null && rules.isNativeType(type) -> if (type.isInstance(value)) {
                    value as T
                } else {
                    fail("Failed to convert value of type ${typeOfValue()} to ${type.simpleName}")
                }
                else -> deserializer.deserialize(this)
            }
        }
        if (rules is Rules.Firestore && descriptor.baseName() == "kotlin.time.Instant") {
            val timestamp = value as? Timestamp ?: fail("Failed to convert value of type ${typeOfValue()} to Instant")
            return Instant.fromEpochSeconds(timestamp.seconds, timestamp.nanoseconds) as T
        }
        return deserializer.deserialize(this)
    }
}

/** A decoder for the elements of a structure. */
internal abstract class ElementDecoder(protected val format: FirebaseFormat) : CompositeDecoder {
    override val serializersModule: SerializersModule get() = format.serializersModule

    protected abstract fun elementDecoder(descriptor: SerialDescriptor, index: Int): PlainDecoder

    override fun decodeSequentially(): Boolean = false

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean = elementDecoder(descriptor, index).decodeBoolean()

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte = elementDecoder(descriptor, index).decodeByte()

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short = elementDecoder(descriptor, index).decodeShort()

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int = elementDecoder(descriptor, index).decodeInt()

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long = elementDecoder(descriptor, index).decodeLong()

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float = elementDecoder(descriptor, index).decodeFloat()

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double = elementDecoder(descriptor, index).decodeDouble()

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char = elementDecoder(descriptor, index).decodeChar()

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String = elementDecoder(descriptor, index).decodeString()

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder = elementDecoder(descriptor, index)

    override fun <T> decodeSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>, previousValue: T?): T = elementDecoder(descriptor, index).decodeSerializableValue(deserializer)

    override fun <T : Any> decodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>, previousValue: T?): T? = elementDecoder(descriptor, index).decodeNullableSerializableValue(deserializer)

    override fun endStructure(descriptor: SerialDescriptor) {}
}

/** A class, read from a map of its properties: the counterpart of the Android SDK's `BeanMapper.deserialize`. */
internal class ClassDecoder(
    format: FirebaseFormat,
    private val map: Map<*, *>,
    private val path: ErrorPath,
    private val documentReference: Any?,
    private val mapping: ClassMapping,
    private val ignoredKey: String?,
) : ElementDecoder(format) {
    private val rules = format.rules
    private var index = 0

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (index < descriptor.elementsCount) {
            val element = index++
            val elementDescriptor = descriptor.getElementDescriptor(element)
            val optional = descriptor.isElementOptional(element)
            if (mapping.documentId[element]) {
                if (documentReference == null) continue
                val name = mapping.names[element]
                if (map.containsKey(name) && rules is Rules.Firestore) {
                    throw rules.mappingError(
                        "'$name' was found from document ${rules.bindings.documentPath(documentReference)}, cannot apply @DocumentId on this property for class ${mapping.className}",
                    )
                }
                return element
            }
            val name = mapping.names[element]
            if (!mapping.excluded[element] && map.containsKey(name)) {
                // A null for a property that cannot hold it keeps the property's default; Java would store the null
                if (map[name] == null && !elementDescriptor.isNullable && optional) continue
                return element
            }
            // A property missing from the data keeps its default, or (without one) Java's zero value: null for a
            // nullable property and zero for a primitive. For any other property kotlinx.serialization reports it missing.
            if (optional) continue
            if (elementDescriptor.isNullable || elementDescriptor.kind.isJavaPrimitive()) return element
        }
        checkUnknownProperties()
        return DECODE_DONE
    }

    private fun checkUnknownProperties() {
        if (mapping.unknownProperties == UnknownProperties.IGNORE) return
        val known = mapping.names.filterIndexed { index, _ -> !mapping.excluded[index] }.toSet()
        for (key in map.keys) {
            if (key in known || key == ignoredKey) continue
            val message = mapping.unknownPropertyMessage(key.toString())
            if (mapping.unknownProperties == UnknownProperties.THROW) throw rules.mappingError(message)
            rules.unknownPropertyLog?.invoke(message)
        }
    }

    override fun elementDecoder(descriptor: SerialDescriptor, index: Int): PlainDecoder {
        val elementDescriptor = descriptor.getElementDescriptor(index)
        val name = mapping.names[index]
        if (mapping.documentId[index] && documentReference != null && rules is Rules.Firestore) {
            val id = if (elementDescriptor.kind == PrimitiveKind.STRING) rules.bindings.documentId(documentReference) else documentReference
            return PlainDecoder(format, id, path.child(name), documentReference)
        }
        return if (!mapping.excluded[index] && map.containsKey(name)) {
            PlainDecoder(format, map[name], path.child(name), documentReference, nullable = elementDescriptor.isNullable)
        } else {
            PlainDecoder(format, null, path.child(name), documentReference, nullable = elementDescriptor.isNullable, absent = true)
        }
    }
}

private fun SerialKind.isJavaPrimitive() = this is PrimitiveKind && this != PrimitiveKind.STRING

internal class ListDecoder(format: FirebaseFormat, private val list: List<*>, private val path: ErrorPath, private val documentReference: Any?) : ElementDecoder(format) {
    private var index = 0

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = list.size

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = if (index < list.size) index++ else DECODE_DONE

    override fun elementDecoder(descriptor: SerialDescriptor, index: Int) = PlainDecoder(
        format,
        list[index],
        path.child("[$index]"),
        documentReference,
        nullable = descriptor.getElementDescriptor(0).isNullable,
    )
}

/** A map: the elements alternate between keys and values. */
internal class MapDecoder(format: FirebaseFormat, map: Map<*, *>, private val path: ErrorPath, private val documentReference: Any?) : ElementDecoder(format) {
    private val entries = map.entries.toList()
    private var index = 0

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = entries.size

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = if (index < entries.size * 2) index++ else DECODE_DONE

    override fun elementDecoder(descriptor: SerialDescriptor, index: Int): PlainDecoder {
        val entry = entries[index / 2]
        return if (index % 2 == 0) {
            PlainDecoder(format, entry.key, path, documentReference)
        } else {
            PlainDecoder(format, entry.value, path.child(entry.key.toString()), documentReference, nullable = descriptor.getElementDescriptor(1).isNullable)
        }
    }
}

/** A polymorphic value, read from the value's map, whose `type` property is the serial name of its class. */
internal class PolymorphicDecoder(format: FirebaseFormat, private val map: Map<*, *>, private val path: ErrorPath, private val documentReference: Any?) : ElementDecoder(format) {
    private var index = 0

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = if (index < 2) index++ else DECODE_DONE

    override fun elementDecoder(descriptor: SerialDescriptor, index: Int): PlainDecoder = when (index) {
        0 -> PlainDecoder(format, map[POLYMORPHIC_TYPE], path.child(POLYMORPHIC_TYPE), documentReference)
        else -> PlainDecoder(format, map, path, documentReference, ignoredKey = POLYMORPHIC_TYPE)
    }
}
