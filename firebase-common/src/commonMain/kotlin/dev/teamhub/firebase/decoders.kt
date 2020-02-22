package dev.teamhub.firebase

import kotlinx.serialization.*
import kotlinx.serialization.CompositeDecoder.Companion.READ_ALL
import kotlinx.serialization.CompositeDecoder.Companion.READ_DONE
import kotlinx.serialization.internal.UnitDescriptor
import kotlinx.serialization.internal.nullable
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.getContextualOrDefault
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
inline fun <reified T> decode(strategy: DeserializationStrategy<T> = EmptyModule.getContextualOrDefault(T::class as KClass<Any>).run { if(null is T) nullable else this } as DeserializationStrategy<T>, value: Any?): T {
    require(value != null || strategy.descriptor.isNullable) { "Value was null for non-nullable type ${T::class}" }
    return FirebaseDecoder(value).decode(strategy)
}

expect fun FirebaseDecoder.structureDecoder(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder

class FirebaseDecoder(internal val value: Any?) : Decoder {

    override val context: SerialModule
        get() = EmptyModule

    override val updateMode: UpdateMode = UpdateMode.BANNED

    @Suppress("UNCHECKED_CAST")
    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>) = structureDecoder(desc, *typeParams)

    override fun decodeString() = decodeString(value)

    override fun decodeDouble() = decodeDouble(value)

    override fun decodeLong() = decodeLong(value)

    override fun decodeByte() = decodeByte(value)

    override fun decodeFloat() = decodeFloat(value)

    override fun decodeInt() = decodeInt(value)

    override fun decodeShort() = decodeShort(value)

    override fun decodeBoolean() = decodeBoolean(value)

    override fun decodeChar() = decodeChar(value)

    override fun decodeEnum(enumDescription: SerialDescriptor) = decodeEnum(value, enumDescription)

    override fun decodeNotNullMark() = decodeNotNullMark(value)

    override fun decodeNull() = decodeNull(value)

    override fun decodeUnit() = decodeUnit(value)

}

class FirebaseClassDecoder(
    size: Int,
    private val containsKey: (name: String) -> Boolean,
    get: (desc: SerialDescriptor, index: Int) -> Any?
) : FirebaseCompositeDecoder(size, get) {
    private var index: Int = 0

    override fun decodeElementIndex(desc: SerialDescriptor): Int =
        (index until desc.elementsCount)
            .firstOrNull { !desc.isElementOptional(it) || containsKey(desc.getElementName(it)) }
            ?.also { index = it + 1 }
            ?: READ_DONE
}

open class FirebaseCompositeDecoder constructor(
    private val size: Int,
    private val get: (desc: SerialDescriptor, index: Int) -> Any?
): CompositeDecoder {

    override val context = EmptyModule
    override val updateMode = UpdateMode.OVERWRITE

    override fun decodeElementIndex(desc: SerialDescriptor) = READ_ALL

    override fun decodeCollectionSize(desc: SerialDescriptor) = size

    override fun <T> decodeSerializableElement(desc: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>): T =
        deserializer.deserialize(FirebaseDecoder(get(desc, index)))

    override fun <T : Any> decodeNullableSerializableElement(desc: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>): T? =
        if(decodeNotNullMark(get(desc, index))) decodeSerializableElement(desc, index, deserializer) else decodeNull(get(desc, index))

    fun decodeNullableSerializableElement(index: Int): Any? = get(UnitDescriptor, index)?.let { value ->
        value.firebaseSerializer().let { decodeSerializableElement(it.descriptor, index, it) }
    }

    override fun <T> updateSerializableElement(desc: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>, old: T): T =
        deserializer.deserialize(FirebaseDecoder(get(desc, index)))

    override fun <T : Any> updateNullableSerializableElement(desc: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>, old: T?): T? =
        if(decodeNotNullMark(get(desc, index))) decodeSerializableElement(desc, index, deserializer) else decodeNull(get(desc, index))

    override fun decodeBooleanElement(desc: SerialDescriptor, index: Int) = decodeBoolean(get(desc, index))

    override fun decodeByteElement(desc: SerialDescriptor, index: Int) = decodeByte(get(desc, index))

    override fun decodeCharElement(desc: SerialDescriptor, index: Int) = decodeChar(get(desc, index))

    override fun decodeDoubleElement(desc: SerialDescriptor, index: Int) = decodeDouble(get(desc, index))

    override fun decodeFloatElement(desc: SerialDescriptor, index: Int) = decodeFloat(get(desc, index))

    override fun decodeIntElement(desc: SerialDescriptor, index: Int) = decodeInt(get(desc, index))

    override fun decodeLongElement(desc: SerialDescriptor, index: Int) = decodeLong(get(desc, index))

    override fun decodeShortElement(desc: SerialDescriptor, index: Int) = decodeShort(get(desc, index))

    override fun decodeStringElement(desc: SerialDescriptor, index: Int) = decodeString(get(desc, index))

    override fun decodeUnitElement(desc: SerialDescriptor, index: Int) = decodeUnit(get(desc, index))

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

private fun decodeEnum(value: Any?, enumDescription: SerialDescriptor) = when(value) {
    is Number -> value.toInt()
    is String -> enumDescription.getElementIndexOrThrow(value)
    else -> throw SerializationException("Expected $value to be enum")
}

private fun decodeNotNullMark(value: Any?) = value != null

private fun decodeNull(value: Any?) = value as Nothing?

private fun decodeUnit(value: Any?) = value as Unit

