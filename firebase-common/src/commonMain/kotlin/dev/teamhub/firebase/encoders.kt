/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.teamhub.firebase

import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptyModule

@InternalSerializationApi
fun <T> encode(strategy: SerializationStrategy<T>, value: T, positiveInfinity: Any = Double.POSITIVE_INFINITY): Any? =
    FirebaseEncoder(positiveInfinity).apply { encode(strategy, value) }.value//.also { println("encoded $it") }

@InternalSerializationApi
@ImplicitReflectionSerializer
fun encode(value: Any?, positiveInfinity: Any = Double.POSITIVE_INFINITY): Any? = value?.let {
    FirebaseEncoder(positiveInfinity).apply { encode(it.firebaseSerializer(), it) }.value
}

@InternalSerializationApi
expect fun FirebaseEncoder.structureEncoder(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder

@InternalSerializationApi
class FirebaseEncoder(positiveInfinity: Any) : TimestampEncoder(positiveInfinity), Encoder {

    var value: Any? = null

    override val context = EmptyModule

    override fun beginStructure(descriptor: SerialDescriptor, vararg typeSerializers: KSerializer<*>) = structureEncoder(descriptor, *typeSerializers)

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

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        this.value = enumDescriptor.getElementName(index)
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

@InternalSerializationApi
@ImplicitReflectionSerializer
open class FirebaseCompositeEncoder constructor(
    positiveInfinity: Any,
    private val end: () -> Unit = {},
    private val set: (desc: SerialDescriptor, index: Int, value: Any?) -> Unit
): TimestampEncoder(positiveInfinity), CompositeEncoder {

    override val context = EmptyModule

//    private fun <T> SerializationStrategy<T>.toFirebase(): SerializationStrategy<T> = when(descriptor.kind) {
//        StructureKind.MAP -> FirebaseMapSerializer<Any>(descriptor.getElementDescriptor(1)) as SerializationStrategy<T>
//        StructureKind.LIST -> FirebaseListSerializer<Any>(descriptor.getElementDescriptor(0)) as SerializationStrategy<T>
//        else -> this
//    }

    override fun endStructure(descriptor: SerialDescriptor) = end()

    override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) =
        set(descriptor, index, value?.let { FirebaseEncoder(positiveInfinity).apply { encode(serializer, value) }.value })

    override fun <T> encodeSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T)  =
        set(descriptor, index, FirebaseEncoder(positiveInfinity).apply { encode(serializer, value) }.value)

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) = set(descriptor, index, value)

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) = set(descriptor, index, value)

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) = set(descriptor, index, value)

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double)  = set(descriptor, index, encodeTimestamp(value))

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) = set(descriptor, index, value)

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) = set(descriptor, index, value)

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) = set(descriptor, index, value)

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short)  = set(descriptor, index, value)

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String)  = set(descriptor, index, value)

    override fun encodeUnitElement(descriptor: SerialDescriptor, index: Int) = set(descriptor, index, Unit)
}
