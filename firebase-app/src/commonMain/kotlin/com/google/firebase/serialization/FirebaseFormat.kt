/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalSerializationApi::class)

package com.google.firebase.serialization

import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * The multiplatform counterpart of the class mappers inside the Firebase Android SDK: Firestore's and Realtime Database's
 * `CustomClassMapper` and Cloud Functions' `Serializer`.
 *
 * The Android SDK maps custom classes by reflection. Reflection is not available on every platform, so this format maps
 * classes through their kotlinx.serialization serializers instead, while following the Android mappers' rules so that
 * code written for the Android SDK needs as few changes as possible: usually just `@Serializable` on each model class.
 *
 * A format converts between Kotlin values and the *plain values* that the Firebase SDKs of every platform accept and
 * return: `null`, [Boolean], [String], numbers, [List]s, [Map]s with [String] keys and the product's own value types
 * (such as a Firestore `GeoPoint`), which are passed through unchanged.
 *
 * - The property names are those the Android SDK derives from a class's getters: a Kotlin property `isActive` is stored
 *   as `active`, and a leading run of capitals is lower-cased (`URL` is stored as `url`).
 * - The value rules, and the exception types and messages when a value is rejected, are those of the Android mapper of
 *   each product: see [Firestore], [Database] and [Functions].
 * - The product's mapping annotations (`@PropertyName`, `@Exclude`, `@IgnoreExtraProperties`, `@ThrowOnExtraProperties`
 *   and, for Firestore, `@ServerTimestamp` and `@DocumentId`) take effect when kotlinx.serialization records them: see
 *   [MappingAnnotations].
 *
 * Where kotlinx.serialization can do more than the Android mapper, the format keeps the extra support rather than
 * reproducing an Android failure that no working Android code depends on: generic classes, `object`s, value classes and
 * sealed class hierarchies (stored with a `type` property holding the subclass's serial name).
 */
public sealed class FirebaseFormat(
    override val serializersModule: SerializersModule,
) : SerialFormat {

    internal abstract val rules: Rules

    /**
     * Converts [value] to plain values by its runtime type, as the Android SDK's mapper does with the `Object` given to
     * calls such as `DocumentReference.set(Object)`: plain values are checked and copied, the product's own value types
     * are passed through and any other object is encoded with the serializer of its class, which must be `@Serializable`.
     */
    public fun convertToPlainValue(value: Any?): Any? = encodeRuntimeValue(this, value, ErrorPath.EMPTY)

    /** Encodes [value] to plain values with [serializer]. */
    public fun <T> encodeToPlainValue(serializer: SerializationStrategy<T>, value: T): Any? = PlainEncoder(this, ErrorPath.EMPTY).apply { encodeSerializableValue(serializer, value) }.value

    /**
     * Decodes plain values to a `T` with [deserializer], as the Android SDK's mapper does in calls such as
     * `DocumentSnapshot.toObject(Class)`. For Firestore, [documentReference] is the document that `@DocumentId`
     * properties receive (its id for a [String] property); it is ignored by the other products.
     */
    public fun <T> decodeFromPlainValue(deserializer: DeserializationStrategy<T>, value: Any?, documentReference: Any? = null): T = PlainDecoder(this, value, ErrorPath.EMPTY, documentReference).decodeSerializableValue(deserializer)

    /**
     * Decodes plain values to an instance of [type], found by its serializer, as the Android SDK's
     * `CustomClassMapper.convertToCustomClass(Object, Class, DocumentReference)` does; `null` stays `null`.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> convertToCustomClass(value: Any?, type: KClass<T>, documentReference: Any? = null): T? {
        if (value == null) return null
        return decodeFromPlainValue(serializerOf(type) as KSerializer<T>, value, documentReference)
    }

    /**
     * The serializer for [type], where [Any] (as in `Map<String, Any?>`) stands for the plain value itself, as `Object`
     * does for the Android mapper: it is decoded as is and encoded by its runtime type.
     */
    @Suppress("UNCHECKED_CAST")
    public fun serializerFor(type: KType): KSerializer<Any?> {
        val arguments = type.arguments.map { it.type ?: typeOf<Any?>() }
        val serializer: KSerializer<*> = when (type.classifier) {
            Any::class -> ContextualSerializer(Any::class)
            Map::class, HashMap::class, LinkedHashMap::class, MutableMap::class ->
                MapSerializer(serializerFor(arguments[0]), serializerFor(arguments[1]))
            List::class, ArrayList::class, MutableList::class ->
                ListSerializer(serializerFor(arguments[0]))
            Set::class, HashSet::class, LinkedHashSet::class, MutableSet::class -> SetSerializer(serializerFor(arguments[0]))
            else -> serializersModule.serializer(type)
        }
        return (if (type.isMarkedNullable) (serializer as KSerializer<Any>).nullable else serializer) as KSerializer<Any?>
    }

    internal fun serializerOf(type: KClass<*>): KSerializer<Any?>? = try {
        serializersModule.serializer(type, emptyList(), false)
    } catch (_: SerializationException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    /**
     * Firestore, following `com.google.firebase.firestore.util.CustomClassMapper`.
     *
     * - Numbers: [Int], [Long], [Float] and [Double]; other numbers and [Char]s are rejected.
     * - Collections: [List]s, and [Map]s with [String] keys; sets, other collections and arrays are rejected.
     * - Enums: the entry's name, or the entry's `@PropertyName`.
     * - `com.google.firebase.Timestamp` and the [Bindings.nativeTypes] (such as `GeoPoint`, `Blob`, `DocumentReference`
     *   and `FieldValue`) are passed through unchanged, and a `kotlin.time.Instant` is stored as a `Timestamp`. In a
     *   `@Serializable` class such properties need kotlinx.serialization's `@Contextual`, or a file-level
     *   `@UseContextualSerialization`.
     * - A `@ServerTimestamp` property that is `null` is written as `FieldValue.serverTimestamp()`, and a `@DocumentId`
     *   property is skipped on writes and receives the document on reads.
     * - Rejected values throw an [IllegalArgumentException] (`Could not serialize object. …`) on writes and a
     *   [RuntimeException] (`Could not deserialize object. …`) on reads, naming the path of the offending field.
     * - A document property without a class property is logged unless the class has `@IgnoreExtraProperties`, and fails
     *   the read if it has `@ThrowOnExtraProperties`.
     */
    public class Firestore(
        public val bindings: Bindings,
        serializersModule: SerializersModule = EmptySerializersModule(),
    ) : FirebaseFormat(serializersModule) {

        /** The Firestore types this format passes through; provided by the Firestore module. */
        public interface Bindings {
            /** The annotations the format honours. */
            public val annotations: MappingAnnotations

            /**
             * The value types passed through unchanged (`Blob`, `DocumentReference`, `FieldValue`, `GeoPoint`, `VectorValue`
             * and the BSON types, and `java.util.Date` on Android); `com.google.firebase.Timestamp` always is.
             */
            public val nativeTypes: List<KClass<*>>

            /** `DocumentReference`, which a `@DocumentId` property may have as its type. */
            public val documentReferenceType: KClass<*>

            /** The id of [documentReference], the value of a [String] `@DocumentId` property. */
            public fun documentId(documentReference: Any): String

            /** The path of [documentReference], for error messages. */
            public fun documentPath(documentReference: Any): String

            /** `FieldValue.serverTimestamp()`, written for a `@ServerTimestamp` property that is `null`. */
            public fun serverTimestamp(): Any
        }

        override val rules: Rules = Rules.Firestore(bindings)
    }

    /**
     * Realtime Database, following `com.google.firebase.database.core.utilities.encoding.CustomClassMapper`.
     *
     * - Numbers: [Int], [Long], [Float] and [Double], where a [Float] or [Double] with an integral value is stored as a
     *   [Long] (`1.0` is stored as `1`); other numbers and [Char]s are rejected.
     * - Collections: [List]s, and [Map]s with [String] keys; sets, other collections and arrays are rejected.
     * - Enums: the entry's name.
     * - Rejected values throw the exception made by [exception] (the module's `DatabaseException`), without a path.
     * - A database property without a class property is logged unless the class has `@IgnoreExtraProperties`, and fails
     *   the read if it has `@ThrowOnExtraProperties`.
     */
    public class Database(
        public val annotations: MappingAnnotations,
        public val exception: (message: String) -> RuntimeException,
        serializersModule: SerializersModule = EmptySerializersModule(),
    ) : FirebaseFormat(serializersModule) {
        override val rules: Rules = Rules.Database(annotations, exception)
    }

    /**
     * Cloud Functions, following `com.google.firebase.functions.Serializer`.
     *
     * [convertToPlainValue] accepts what the Android SDK accepts as call data: `null`, [Boolean], [String], any number,
     * [List]s and [Map]s with [String] keys; anything else throws an [IllegalArgumentException]
     * (`Object cannot be encoded in JSON: …`). The Android SDK has no class mapping for Cloud Functions, so a class is
     * only mapped through its serializer ([encodeToPlainValue], [decodeFromPlainValue]), with its properties' serial names
     * as they are and unknown properties ignored.
     */
    public class Functions(
        serializersModule: SerializersModule = EmptySerializersModule(),
    ) : FirebaseFormat(serializersModule) {
        override val rules: Rules = Rules.Functions
    }
}

/**
 * The mapping annotations of a Firebase product, as declared by its module (for example
 * `com.google.firebase.firestore.PropertyName`).
 *
 * kotlinx.serialization only records annotations that are meta-annotated with `@SerialInfo` and applied to the property
 * itself (not with a `@get:`/`@field:` use-site target), or to the class; an annotation it does not record has no effect.
 */
public class MappingAnnotations(
    /** The name given by a `@PropertyName` annotation, or `null` for any other annotation. */
    public val propertyName: (Annotation) -> String? = { null },
    /** `@Exclude`: the property is neither written nor read. */
    public val exclude: KClass<out Annotation>? = null,
    /** `@IgnoreExtraProperties`: properties without a class property are ignored silently. */
    public val ignoreExtraProperties: KClass<out Annotation>? = null,
    /** `@ThrowOnExtraProperties`: properties without a class property fail the read. */
    public val throwOnExtraProperties: KClass<out Annotation>? = null,
    /** Firestore's `@ServerTimestamp`. */
    public val serverTimestamp: KClass<out Annotation>? = null,
    /** Firestore's `@DocumentId`. */
    public val documentId: KClass<out Annotation>? = null,
)

/** Encodes [value] to plain values with the serializer of its type, where [Any] stands for any plain value. */
public inline fun <reified T> FirebaseFormat.encodeToPlainValue(value: T): Any? = encodeToPlainValue(serializerFor(typeOf<T>()), value)

/** Decodes plain values to a `T` with the serializer of its type, where [Any] stands for any plain value. */
@Suppress("UNCHECKED_CAST")
public inline fun <reified T> FirebaseFormat.decodeFromPlainValue(value: Any?, documentReference: Any? = null): T = decodeFromPlainValue(serializerFor(typeOf<T>()), value, documentReference) as T
