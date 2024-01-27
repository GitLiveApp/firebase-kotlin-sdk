/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

@Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("encode(strategy, value) { encodeDefaults = shouldEncodeElementDefault }"))
fun <T> encode(strategy: SerializationStrategy<T>, value: T, shouldEncodeElementDefault: Boolean): Any? = encode(strategy, value) {
    this.encodeDefaults = shouldEncodeElementDefault
}

inline fun <T> encode(strategy: SerializationStrategy<T>, value: T, buildSettings: EncodeSettings.Builder.() -> Unit) =
    encode(strategy, value, EncodeSettings.BuilderImpl().apply(buildSettings).buildEncodeSettings())

@PublishedApi
internal inline fun <T> encode(strategy: SerializationStrategy<T>, value: T, encodeSettings: EncodeSettings): Any? =
    FirebaseEncoder(encodeSettings).apply { encodeSerializableValue(strategy, value) }.value

@Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("encode(value) { this.encodeDefaults = shouldEncodeElementDefault }"))
inline fun <reified T> encode(value: T, shouldEncodeElementDefault: Boolean): Any? = encode(value) {
    this.encodeDefaults = shouldEncodeElementDefault
}

inline fun <reified T> encode(value: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) =
    encode(value, EncodeSettings.BuilderImpl().apply(buildSettings).buildEncodeSettings())

@PublishedApi
internal inline fun <reified T> encode(value: T, encodeSettings: EncodeSettings): Any? = value?.let {
    FirebaseEncoder(encodeSettings).apply {
        if (it is ValueWithSerializer<*> && it.value is T) {
            @Suppress("UNCHECKED_CAST")
            (it as ValueWithSerializer<T>).let {
                encodeSerializableValue(it.serializer, it.value)
            }
        } else {
            encodeSerializableValue(it.firebaseSerializer(), it)
        }
    }.value
}

/**
 * An extension which which serializer to use for value. Handy in updating fields by name or path
 * where using annotation is not possible
 * @return a value with a custom serializer.
 */
fun <T> T.withSerializer(serializer: SerializationStrategy<T>): Any = ValueWithSerializer(this, serializer)
data class ValueWithSerializer<T>(val value: T, val serializer: SerializationStrategy<T>)

expect fun FirebaseEncoder.structureEncoder(descriptor: SerialDescriptor): FirebaseCompositeEncoder

class FirebaseEncoder(
    internal val settings: EncodeSettings
) : Encoder {

    constructor(shouldEncodeElementDefault: Boolean) : this(
        EncodeSettings.BuilderImpl().apply { this.encodeDefaults = shouldEncodeElementDefault }.buildEncodeSettings()
    )

    var value: Any? = null

    internal val shouldEncodeElementDefault = settings.encodeDefaults
    override val serializersModule: SerializersModule = settings.serializersModule

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

    override fun encodeInline(descriptor: SerialDescriptor): Encoder =
        FirebaseEncoder(settings)

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        encodePolymorphically(serializer, value) {
            polymorphicDiscriminator = it
        }
    }
}

open class FirebaseCompositeEncoder constructor(
    private val settings: EncodeSettings,
    private val end: () -> Unit = {},
    private val setPolymorphicType: (String, String) -> Unit = { _, _ -> },
    private val set: (descriptor: SerialDescriptor, index: Int, value: Any?) -> Unit,
): CompositeEncoder {

//    private fun <T> SerializationStrategy<T>.toFirebase(): SerializationStrategy<T> = when(descriptor.kind) {
//        StructureKind.MAP -> FirebaseMapSerializer<Any>(descriptor.getElementDescriptor(1)) as SerializationStrategy<T>
//        StructureKind.LIST -> FirebaseListSerializer<Any>(descriptor.getElementDescriptor(0)) as SerializationStrategy<T>
//        else -> this
//    }

    override val serializersModule: SerializersModule = settings.serializersModule

    override fun endStructure(descriptor: SerialDescriptor) = end()

    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int) = settings.encodeDefaults

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) = set(
        descriptor,
        index,
        value?.let {
            FirebaseEncoder(settings).apply {
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
        FirebaseEncoder(settings).apply {
            encodeSerializableValue(serializer, value)
        }.value
    )

    fun <T> encodeObject(descriptor: SerialDescriptor, index: Int, value: T) = set(descriptor, index, value)

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
        FirebaseEncoder(settings)

    fun encodePolymorphicClassDiscriminator(discriminator: String, type: String) {
        setPolymorphicType(discriminator, type)
    }
}
