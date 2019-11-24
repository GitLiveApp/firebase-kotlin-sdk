package dev.teamhub.firebase

import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.getContextualOrDefault
import kotlin.reflect.KClass

object Mapper : AbstractSerialFormat(EmptyModule) {

    internal class OutMapper : NamedValueEncoder() {

        override fun beginCollection(
            desc: SerialDescriptor,
            collectionSize: Int,
            vararg typeParams: KSerializer<*>
        ): CompositeEncoder {
            encodeTaggedInt(nested("size"), collectionSize)
            return this
        }

        private var _map: MutableMap<String, Any> = mutableMapOf()

        val map: Map<String, Any>
            get() = _map

        override fun encodeTaggedValue(tag: String, value: Any) {
            _map[tag] = value
        }

        override fun encodeTaggedNull(tag: String) {
            throw SerializationException("null is not supported. use Mapper.mapNullable()/OutNullableMapper instead")
        }
    }

    internal class InMapper(private val map: Map<String, Any>) : NamedValueDecoder() {

        override fun decodeCollectionSize(desc: SerialDescriptor): Int {
            return decodeTaggedInt(nested("size"))
        }

        override fun decodeTaggedString(tag: String) = decodeTaggedValue(tag).toString()

        override fun decodeTaggedDouble(tag: String) = when(val value = decodeTaggedValue(tag)) {
            is Number -> value.toDouble()
            is String -> value.toDouble()
            else -> super.decodeTaggedDouble(tag)
        }

        override fun decodeTaggedLong(tag: String) = when(val value = decodeTaggedValue(tag)) {
            is Number -> value.toLong()
            is String -> value.toLong()
            else -> super.decodeTaggedLong(tag)
        }

        override fun decodeTaggedByte(tag: String) = when(val value = decodeTaggedValue(tag)) {
            is Number -> value.toByte()
            is String -> value.toByte()
            else -> super.decodeTaggedByte(tag)
        }

        override fun decodeTaggedFloat(tag: String) = when(val value = decodeTaggedValue(tag)) {
            is Number -> value.toFloat()
            is String -> value.toFloat()
            else -> super.decodeTaggedFloat(tag)
        }

        override fun decodeTaggedInt(tag: String) = when(val value = decodeTaggedValue(tag)) {
            is Number -> value.toInt()
            is String -> value.toInt()
            else -> super.decodeTaggedInt(tag)
        }

        override fun decodeTaggedShort(tag: String) = when(val value = decodeTaggedValue(tag)) {
            is Number -> value.toShort()
            is String -> value.toShort()
            else -> super.decodeTaggedShort(tag)
        }

        override fun decodeTaggedValue(tag: String): Any = map.getValue(tag)
    }

    fun <T> map(strategy: SerializationStrategy<T>, obj: T): Map<String, Any> {
        val m = OutMapper()
        m.encode(strategy, obj)
        return m.map
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> decode(strategy: DeserializationStrategy<T>, value: Any?): T = value?.let {
        when(strategy) {
            is BooleanSerializer -> value
            is StringSerializer -> value.toString()
            is IntSerializer -> when (value) {
                is Number -> value.toInt()
                is String -> value.toInt()
                else -> value
            }
            is ShortSerializer -> when (value) {
                is Number -> value.toShort()
                is String -> value.toShort()
                else -> value
            }
            is LongSerializer -> when (value) {
                is Number -> value.toLong()
                is String -> value.toLong()
                else -> value
            }
            is FloatSerializer -> when (value) {
                is Number -> value.toFloat()
                is String -> value.toFloat()
                else -> value
            }
            is ByteSerializer -> when (value) {
                is Number -> value.toByte()
                is String -> value.toByte()
                else -> value
            }
            is DoubleSerializer -> when (value) {
                is Number -> value.toDouble()
                is String -> value.toDouble()
                else -> value
            }
            is CharSerializer -> when (value) {
                is Number -> value.toChar()
                is String -> value.takeIf { it.length == 1 }?.let { it[0] } ?: value
                else -> value
            }
            else -> InMapper(value as Map<String, Any>).decode(strategy)
        }
    } as T

    @ImplicitReflectionSerializer
    inline fun <reified T : Any> map(obj: T): Map<String, Any> = map(context.getContextualOrDefault(T::class), obj)

    @ImplicitReflectionSerializer
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> decode(value: Any?): T = decode(context.getContextualOrDefault(T::class as KClass<*>) as DeserializationStrategy<T>, value)

}

