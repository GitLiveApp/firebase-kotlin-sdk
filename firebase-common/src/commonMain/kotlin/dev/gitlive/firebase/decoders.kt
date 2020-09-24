/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.*
import kotlinx.serialization.CompositeDecoder.Companion.READ_DONE
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.getContextualOrDefault
import kotlin.reflect.KClass

@ImplicitReflectionSerializer
@Suppress("UNCHECKED_CAST")
inline fun <reified T> decode(value: Any?): T {
    val strategy = EmptyModule.getContextualOrDefault(T::class as KClass<*>).run { if (null is T) nullable else this }
    return decode(strategy as DeserializationStrategy<T>, value)
}

fun <T> decode(strategy: DeserializationStrategy<T>, value: Any?): T {
    require(value != null || strategy.descriptor.isNullable) { "Value was null for non-nullable type ${strategy.descriptor.serialName}" }
    return FirebaseDecoder(value).decode(strategy)
}

expect fun FirebaseDecoder.structureDecoder(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder

class FirebaseDecoder(internal val value: Any?) : Decoder {

    override val context: SerialModule
        get() = EmptyModule

    override val updateMode: UpdateMode = UpdateMode.BANNED

    @Suppress("UNCHECKED_CAST")
    override fun beginStructure(descriptor: SerialDescriptor, vararg typeParams: KSerializer<*>) = structureDecoder(descriptor, *typeParams)

    override fun decodeString() = decodeString(value)

    override fun decodeDouble() = decodeDouble(value)

    override fun decodeLong() = decodeLong(value)

    override fun decodeByte() = decodeByte(value)

    override fun decodeFloat() = decodeFloat(value)

    override fun decodeInt() = decodeInt(value)

    override fun decodeShort() = decodeShort(value)

    override fun decodeBoolean() = decodeBoolean(value)

    override fun decodeChar() = decodeChar(value)

    override fun decodeEnum(enumDescriptor: SerialDescriptor) = decodeEnum(value, enumDescriptor)

    override fun decodeNotNullMark() = decodeNotNullMark(value)

    override fun decodeNull() = decodeNull(value)

    override fun decodeUnit() = decodeUnit(value)

}

class FirebaseClassDecoder(
    size: Int,
    private val containsKey: (name: String) -> Boolean,
    get: (descriptor: SerialDescriptor, index: Int) -> Any?
) : FirebaseCompositeDecoder(size, get) {
    private var index: Int = 0

    override fun decodeSequentially() = false

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        (index until descriptor.elementsCount)
            .firstOrNull { !descriptor.isElementOptional(it) || containsKey(descriptor.getElementName(it)) }
            ?.also { index = it + 1 }
            ?: READ_DONE
}

open class FirebaseEmptyCompositeDecoder(): FirebaseCompositeDecoder(0, { _, _ -> })

open class FirebaseCompositeDecoder constructor(
    private val size: Int,
    private val get: (descriptor: SerialDescriptor, index: Int) -> Any?
): CompositeDecoder {

    override val context = EmptyModule
    override val updateMode = UpdateMode.OVERWRITE

    override fun decodeSequentially() = true

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = throw NotImplementedError()

    override fun decodeCollectionSize(descriptor: SerialDescriptor) = size

    override fun <T> decodeSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>): T =
        deserializer.deserialize(FirebaseDecoder(get(descriptor, index)))

    override fun <T : Any> decodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>): T? =
        if(decodeNotNullMark(get(descriptor, index))) decodeSerializableElement(descriptor, index, deserializer) else decodeNull(get(descriptor, index))

//    fun decodeNullableSerializableElement(index: Int): Any? = get(UnitDescriptor, index)?.let { value ->
//        value.firebaseSerializer().let {  decodeSerializableElement<Any>(it.descriptor, index, it) }
//    }

    override fun <T> updateSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>, old: T): T =
        deserializer.deserialize(FirebaseDecoder(get(descriptor, index)))

    override fun <T : Any> updateNullableSerializableElement(descriptor: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>, old: T?): T? =
        if(decodeNotNullMark(get(descriptor, index))) decodeSerializableElement(descriptor, index, deserializer) else decodeNull(get(descriptor, index))

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int) = decodeBoolean(get(descriptor, index))

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int) = decodeByte(get(descriptor, index))

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int) = decodeChar(get(descriptor, index))

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int) = decodeDouble(get(descriptor, index))

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int) = decodeFloat(get(descriptor, index))

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int) = decodeInt(get(descriptor, index))

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int) = decodeLong(get(descriptor, index))

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int) = decodeShort(get(descriptor, index))

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int) = decodeString(get(descriptor, index))

    override fun decodeUnitElement(descriptor: SerialDescriptor, index: Int) = decodeUnit(get(descriptor, index))

    override fun endStructure(descriptor: SerialDescriptor) {
    }
}

private fun decodeString(value: Any?) = value.toString()

private fun decodeDouble(value: Any?) = when(value) {
    is Number -> value.toDouble()
    is String -> value.toDouble()
    else -> throw SerializationException("Expected $value to be double")
}

private fun decodeLong(value: Any?) = when(value) {
    is Number -> value.toLong()
    is String -> value.toLong()
    else -> throw SerializationException("Expected $value to be long")
}

private fun decodeByte(value: Any?) = when(value) {
    is Number -> value.toByte()
    is String -> value.toByte()
    else -> throw SerializationException("Expected $value to be byte")
}

private fun decodeFloat(value: Any?) = when(value) {
    is Number -> value.toFloat()
    is String -> value.toFloat()
    else -> throw SerializationException("Expected $value to be float")
}

private fun decodeInt(value: Any?) = when(value) {
    is Number -> value.toInt()
    is String -> value.toInt()
    else -> throw SerializationException("Expected $value to be int")
}

private fun decodeShort(value: Any?) = when(value) {
    is Number -> value.toShort()
    is String -> value.toShort()
    else -> throw SerializationException("Expected $value to be short")
}

private fun decodeBoolean(value: Any?) = value as Boolean

private fun decodeChar(value: Any?) = when(value) {
    is Number -> value.toChar()
    is String -> value[0]
    else -> throw SerializationException("Expected $value to be char")
}

private fun decodeEnum(value: Any?, enumDescriptor: SerialDescriptor) = when(value) {
    is Number -> value.toInt()
    is String -> enumDescriptor.getElementIndexOrThrow(value)
    else -> throw SerializationException("Expected $value to be enum")
}

private fun decodeNotNullMark(value: Any?) = value != null

private fun decodeNull(value: Any?) = value as Nothing?

private fun decodeUnit(value: Any?) = value as Unit

