package dev.teamhub.firebase

import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptyModule

fun <T> encode(strategy: SerializationStrategy<T> , value: T, positiveInfinity: Any = Double.POSITIVE_INFINITY): Any? =
    FirebaseEncoder(positiveInfinity).apply { encode(strategy, value) }.value//.also { println("encoded $it") }

@ImplicitReflectionSerializer
fun encode(value: Any?, positiveInfinity: Any = Double.POSITIVE_INFINITY): Any? = value?.let {
    FirebaseEncoder(positiveInfinity).apply { encode(it.firebaseSerializer(), it) }.value
}

expect fun FirebaseEncoder.structureEncoder(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder

class FirebaseEncoder(positiveInfinity: Any) : TimestampEncoder(positiveInfinity), Encoder {

    var value: Any? = null

    override val context = EmptyModule

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>) = structureEncoder(desc, *typeParams)

    override fun encodeBoolean(value: Boolean) {
        this.value = value
    }

    override fun encodeByte(value: Byte) {
        this.value = value
    }

    override fun encodeChar(value: Char) {
        this.value = value
    }

    override fun encodeDouble(value: Double) {
        this.value = encodeTimestamp(value)
    }

    override fun encodeEnum(enumDescription: SerialDescriptor, ordinal: Int) {
        this.value = enumDescription.getElementName(ordinal)
    }

    override fun encodeFloat(value: Float) {
        this.value = value
    }

    override fun encodeInt(value: Int) {
        this.value = value
    }

    override fun encodeLong(value: Long) {
        this.value = value
    }

    override fun encodeNotNullMark() {
        //no-op
    }

    override fun encodeNull() {
        this.value = null
    }

    override fun encodeShort(value: Short) {
        this.value = value
    }

    override fun encodeString(value: String) {
        this.value = value
    }

    override fun encodeUnit() {
        this.value = Unit
    }

}

abstract class TimestampEncoder(internal val positiveInfinity: Any) {
    fun encodeTimestamp(value: Double) = when(value) {
        Double.POSITIVE_INFINITY -> positiveInfinity
        else -> value
    }
}

open class FirebaseCompositeEncoder(
    positiveInfinity: Any,
    private val end: () -> Unit = {},
    private val set: (desc: SerialDescriptor, index: Int, value: Any?) -> Unit
): TimestampEncoder(positiveInfinity), CompositeEncoder {

    override val context = EmptyModule

    override fun endStructure(desc: SerialDescriptor) {
        super.endStructure(desc)
    }

    override fun <T : Any> encodeNullableSerializableElement(desc: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) =
        set(desc, index, value?.let { FirebaseEncoder(positiveInfinity).apply { encode(serializer, value) }.value })

    override fun <T> encodeSerializableElement(desc: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T)  =
        set(desc, index, FirebaseEncoder(positiveInfinity).apply { encode(serializer, value) }.value)

    override fun encodeNonSerializableElement(desc: SerialDescriptor, index: Int, value: Any) = set(desc, index, value)

    override fun encodeBooleanElement(desc: SerialDescriptor, index: Int, value: Boolean) = set(desc, index, value)

    override fun encodeByteElement(desc: SerialDescriptor, index: Int, value: Byte) = set(desc, index, value)

    override fun encodeCharElement(desc: SerialDescriptor, index: Int, value: Char) = set(desc, index, value)

    override fun encodeDoubleElement(desc: SerialDescriptor, index: Int, value: Double)  = set(desc, index, encodeTimestamp(value))

    override fun encodeFloatElement(desc: SerialDescriptor, index: Int, value: Float) = set(desc, index, value)

    override fun encodeIntElement(desc: SerialDescriptor, index: Int, value: Int) = set(desc, index, value)

    override fun encodeLongElement(desc: SerialDescriptor, index: Int, value: Long) = set(desc, index, value)

    override fun encodeShortElement(desc: SerialDescriptor, index: Int, value: Short)  = set(desc, index, value)

    override fun encodeStringElement(desc: SerialDescriptor, index: Int, value: String)  = set(desc, index, value)

    override fun encodeUnitElement(desc: SerialDescriptor, index: Int) = set(desc, index, Unit)

}


