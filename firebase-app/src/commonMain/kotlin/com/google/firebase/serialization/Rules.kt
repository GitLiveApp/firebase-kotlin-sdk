/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.serialization

import com.google.firebase.Timestamp
import kotlin.reflect.KClass

/**
 * What differs between the Android SDK's mappers. The messages are copied from them (firebase-android-sdk
 * `firebase-firestore/.../util/CustomClassMapper.java`, `firebase-database/.../encoding/CustomClassMapper.java` and
 * `firebase-functions/.../Serializer.kt`) so that errors read the same on every platform.
 */
internal sealed class Rules {

    abstract val annotations: MappingAnnotations

    /** Whether class properties are named as the Android SDK names them from getters (see [beanPropertyName]). */
    abstract val beanNames: Boolean

    /** Whether a class needs at least one property ("No properties to serialize found on class …"). */
    abstract val requiresProperties: Boolean

    /** Whether properties without a class property are logged by default; `null` ignores them. */
    abstract val unknownPropertyLog: ((String) -> Unit)?

    abstract fun serializeError(path: ErrorPath, reason: String): RuntimeException

    abstract fun deserializeError(path: ErrorPath, reason: String): RuntimeException

    /** An error in the mapping of a class itself, reported without the "Could not …" prefix. */
    abstract fun mappingError(reason: String): RuntimeException

    /** The plain value for a number, or a failure for a number type the product does not store. */
    abstract fun encodeNumber(value: Number, path: ErrorPath): Any

    /**
     * The plain value for a [Short] or [Byte] (named [type]) encoded by a serializer; checked by name, as on Kotlin/JS a
     * number of any type passes `is Short`.
     */
    open fun encodeSmallNumber(value: Number, type: String, path: ErrorPath): Any = throw serializeError(path, unsupportedNumber(type))

    abstract fun rejectChar(value: Char, path: ErrorPath): Nothing

    /** A collection that is not a [List], such as a [Set]. */
    abstract fun rejectCollection(value: Any?, path: ErrorPath): Nothing

    abstract fun rejectArray(value: Any?, path: ErrorPath): Nothing

    abstract fun rejectMapKey(path: ErrorPath): Nothing

    /** A value [convertToPlainValue][FirebaseFormat.convertToPlainValue] cannot map: no serializer, or no class mapping. */
    abstract fun rejectValue(value: Any, path: ErrorPath): Nothing

    /** Whether [convertToPlainValue][FirebaseFormat.convertToPlainValue] maps enums and classes by runtime type. */
    abstract val mapsObjects: Boolean

    /** The name given to an enum entry by an annotation, as Firestore's `@PropertyName`; the entry's name otherwise. */
    open val enumPropertyName: (Annotation) -> String? = { null }

    /** Types passed through unchanged. */
    open fun isNativeType(type: KClass<*>): Boolean = false

    open fun isNativeValue(value: Any): Boolean = false

    /** The deepest path encoded before the value is taken to be a cycle, or `null` for no limit. */
    open val maxDepth: Int? = null

    /** The message for a value that is not a list where a list is expected. */
    abstract fun expectedList(value: Any): String

    /** The message for decoding to an array. */
    abstract val arrayTarget: String

    /** Firestore */
    class Firestore(val bindings: FirebaseFormat.Firestore.Bindings) : Rules() {
        override val annotations get() = bindings.annotations
        override val beanNames = true
        override val requiresProperties = true
        override val unknownPropertyLog: (String) -> Unit = { logWarning("Firestore", "[CustomClassMapper]: $it") }
        override val mapsObjects = true
        override val maxDepth = 500
        override val enumPropertyName get() = bindings.annotations.propertyName

        override fun serializeError(path: ErrorPath, reason: String) = IllegalArgumentException("Could not serialize object. $reason${path.suffix()}")
        override fun deserializeError(path: ErrorPath, reason: String) = RuntimeException("Could not deserialize object. $reason${path.suffix()}")
        override fun mappingError(reason: String) = RuntimeException(reason)

        override fun encodeNumber(value: Number, path: ErrorPath): Any = when (value) {
            is Long, is Int, is Double, is Float -> value
            else -> throw serializeError(path, unsupportedNumber(value::class.simpleName))
        }

        override fun rejectChar(value: Char, path: ErrorPath) = throw serializeError(path, "Characters are not supported, please use Strings")
        override fun rejectCollection(value: Any?, path: ErrorPath) = throw serializeError(path, "Serializing Collections is not supported, please use Lists instead")
        override fun rejectArray(value: Any?, path: ErrorPath) = throw serializeError(path, "Serializing Arrays is not supported, please use Lists instead")
        override fun rejectMapKey(path: ErrorPath) = throw serializeError(path, "Maps with non-string keys are not supported")
        override fun rejectValue(value: Any, path: ErrorPath) = throw serializeError(path, notSerializable(value))

        override fun isNativeType(type: KClass<*>) = type == Timestamp::class || bindings.nativeTypes.any { it == type }
        override fun isNativeValue(value: Any) = value is Timestamp || bindings.nativeTypes.any { it.isInstance(value) }

        override fun expectedList(value: Any) = "Expected a List, but got a ${javaClassString(value)}"
        override val arrayTarget = "Converting to Arrays is not supported, please use Lists instead"
    }

    /** Realtime Database */
    class Database(override val annotations: MappingAnnotations, val exception: (String) -> RuntimeException) : Rules() {
        override val beanNames = true
        override val requiresProperties = true
        override val unknownPropertyLog: (String) -> Unit = { logWarning("ClassMapper", it) }
        override val mapsObjects = true

        override fun serializeError(path: ErrorPath, reason: String) = exception(reason)
        override fun deserializeError(path: ErrorPath, reason: String) = exception(reason)
        override fun mappingError(reason: String) = exception(reason)

        override fun encodeNumber(value: Number, path: ErrorPath): Any = when (value) {
            // Int first: on Kotlin/JS an integral number passes both `is Int` and `is Double`
            is Long, is Int -> value
            is Float, is Double -> {
                val double = value.toDouble()
                if (double <= Long.MAX_VALUE.toDouble() && double >= Long.MIN_VALUE.toDouble() && kotlin.math.floor(double) == double) value.toLong() else double
            }
            else -> throw serializeError(path, unsupportedNumber(value::class.simpleName))
        }

        override fun rejectChar(value: Char, path: ErrorPath) = throw serializeError(path, "Characters are not supported, please use Strings")
        override fun rejectCollection(value: Any?, path: ErrorPath) = throw serializeError(path, "Serializing Collections is not supported, please use Lists instead")
        override fun rejectArray(value: Any?, path: ErrorPath) = throw serializeError(path, "Serializing Arrays is not supported, please use Lists instead")
        override fun rejectMapKey(path: ErrorPath) = throw serializeError(path, "Maps with non-string keys are not supported")
        override fun rejectValue(value: Any, path: ErrorPath) = throw serializeError(path, notSerializable(value))

        override fun expectedList(value: Any) = "Expected a List while deserializing, but got a ${javaClassString(value)}"

        // "Listsinstead" is the Android SDK's message.
        override val arrayTarget = "Converting to Arrays is not supported, please use Listsinstead"
    }

    /** Cloud Functions */
    object Functions : Rules() {
        override val annotations = MappingAnnotations()
        override val beanNames = false
        override val requiresProperties = false
        override val unknownPropertyLog: ((String) -> Unit)? = null
        override val mapsObjects = false

        override fun serializeError(path: ErrorPath, reason: String) = IllegalArgumentException(reason)
        override fun deserializeError(path: ErrorPath, reason: String) = IllegalArgumentException(reason)
        override fun mappingError(reason: String) = IllegalArgumentException(reason)

        override fun encodeNumber(value: Number, path: ErrorPath): Any = value
        override fun encodeSmallNumber(value: Number, type: String, path: ErrorPath): Any = value

        override fun rejectChar(value: Char, path: ErrorPath) = rejectValue(value, path)
        override fun rejectCollection(value: Any?, path: ErrorPath) = rejectValue(value ?: "null", path)
        override fun rejectArray(value: Any?, path: ErrorPath) = rejectValue(value ?: "null", path)
        override fun rejectMapKey(path: ErrorPath) = throw IllegalArgumentException("Object keys must be strings.")
        override fun rejectValue(value: Any, path: ErrorPath) = throw IllegalArgumentException("Object cannot be encoded in JSON: $value")

        override fun expectedList(value: Any) = "Expected a List, but got a ${javaClassString(value)}"
        override val arrayTarget = "Converting to Arrays is not supported, please use Lists instead"
    }
}

private fun unsupportedNumber(type: String?) = "Numbers of type $type are not supported, please use an int, long, float or double"

private fun notSerializable(value: Any) = "Class ${javaClassName(value)} is not @Serializable. The Android SDK maps classes by reflection; on every platform this format maps them with their kotlinx.serialization serializer"

internal fun isArray(value: Any): Boolean = value is Array<*> || value is IntArray || value is LongArray || value is DoubleArray ||
    value is FloatArray || value is BooleanArray || value is ByteArray || value is ShortArray || value is CharArray
