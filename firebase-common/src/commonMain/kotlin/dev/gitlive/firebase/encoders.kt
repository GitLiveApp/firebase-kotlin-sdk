/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.modules.EmptySerializersModule

fun <T> encode(strategy: SerializationStrategy<T>, value: T, shouldEncodeElementDefault: Boolean): Any? =
    FirebaseEncoder(shouldEncodeElementDefault).apply { encodeSerializableValue(strategy, value) }.value//.also { println("encoded $it") }

inline fun <reified T> encode(value: T, shouldEncodeElementDefault: Boolean): Any? = value?.let {
    FirebaseEncoder(shouldEncodeElementDefault).apply { encodeSerializableValue(it.firebaseSerializer(), it) }.value
}

expect fun FirebaseEncoder.structureEncoder(descriptor: SerialDescriptor): FirebaseCompositeEncoder

class FirebaseEncoder(internal val shouldEncodeElementDefault: Boolean) : Encoder {

    var value: Any? = null

    override val serializersModule = EmptySerializersModule
    private var polymorphicDiscriminator: String? = null

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        val encoder = structureEncoder(descriptor)
        if (polymorphicDiscriminator != null) {
            encoder.encodePolymorphicClassDiscriminator(polymorphicDiscriminator!!, descriptor.serialName)
            polymorphicDiscriminator = null
        }
        return encoder
    }

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
        this.value = value
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

    override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder =
        FirebaseEncoder(shouldEncodeElementDefault)

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        encodePolymorphically(serializer, value) {
            polymorphicDiscriminator = it
        }
    }
}

open class FirebaseCompositeEncoder constructor(
    private val shouldEncodeElementDefault: Boolean,
    private val end: () -> Unit = {},
    private val setPolymorphicType: (String, String) -> Unit = { _, _ -> },
    private val set: (descriptor: SerialDescriptor, index: Int, value: Any?) -> Unit,
): CompositeEncoder {

    override val serializersModule = EmptySerializersModule

//    private fun <T> SerializationStrategy<T>.toFirebase(): SerializationStrategy<T> = when(descriptor.kind) {
//        StructureKind.MAP -> FirebaseMapSerializer<Any>(descriptor.getElementDescriptor(1)) as SerializationStrategy<T>
//        StructureKind.LIST -> FirebaseListSerializer<Any>(descriptor.getElementDescriptor(0)) as SerializationStrategy<T>
//        else -> this
//    }

    override fun endStructure(descriptor: SerialDescriptor) = end()

    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int) = shouldEncodeElementDefault

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) = set(
        descriptor,
        index,
        value?.let {
            FirebaseEncoder(shouldEncodeElementDefault).apply {
                encodeSerializableValue(serializer, value)
            }.value
        }
    )

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) = set(
        descriptor,
        index,
        FirebaseEncoder(shouldEncodeElementDefault).apply {
            encodeSerializableValue(serializer, value)
        }.value
    )

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) = set(descriptor, index, value)

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) = set(descriptor, index, value)

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) = set(descriptor, index, value)

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) = set(descriptor, index, value)

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) = set(descriptor, index, value)

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) = set(descriptor, index, value)

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) = set(descriptor, index, value)

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) = set(descriptor, index, value)

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) = set(descriptor, index, value)

    @ExperimentalSerializationApi
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder =
        FirebaseEncoder(shouldEncodeElementDefault)

    fun encodePolymorphicClassDiscriminator(discriminator: String, type: String) {
        setPolymorphicType(discriminator, type)
    }
}

