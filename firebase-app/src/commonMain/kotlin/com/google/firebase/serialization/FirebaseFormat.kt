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
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleCollector
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
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
 * A format converts between Kotlin values and the *plain values* that the platform's Firebase SDK accepts and returns:
 * `null`, [Boolean], [String], numbers, lists, maps with [String] keys and the product's own value types (such as a
 * Firestore `GeoPoint`), which are passed through unchanged. The lists and maps are the platform's: Kotlin [List]s and
 * [Map]s on Android, the JVM and Apple platforms, whose SDKs exchange them, and JS arrays and objects on JS and wasmJs,
 * whose SDK exchanges those (a Kotlin [List] or [Map] is read there too). So what a format produces can be handed to the
 * SDK, and what the SDK returns can be handed to a format, on every platform.
 *
 * - The property names are the serial names: the Kotlin property name unless `@SerialName` says otherwise, and a
 *   `@Transient` property is left out. (The Android SDK's reflective mappers derive names from getters instead, storing
 *   `isActive` as `active`; a class migrated from them names such properties with `@SerialName`.)
 * - The value rules, and the exception types and messages when a value is rejected, are those of the Android mapper of
 *   each product: see [Firestore], [Database] and [Functions].
 * - The mapping annotations are this package's [IgnoreExtraProperties], [ThrowOnExtraProperties] and, for Firestore,
 *   [ServerTimestamp] and [DocumentId], which kotlinx.serialization records on every platform when they are applied to
 *   the class or the property itself. (The Android SDK's annotations of the same names are the SDK's own classes on
 *   Android, which kotlinx.serialization cannot record, so the product modules deprecate them towards these.)
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
     * The type arguments of a generic class are erased at runtime, so such a value goes through [encodeToPlainValue],
     * whose serializer knows them.
     */
    public fun convertToPlainValue(value: Any?): Any? = toPlatformLeaf(encodeRuntimeValue(this, value, ErrorPath.EMPTY))

    /** Encodes [value] to plain values with [serializer]. */
    public fun <T> encodeToPlainValue(serializer: SerializationStrategy<T>, value: T): Any? = toPlatformLeaf(PlainEncoder(this, ErrorPath.EMPTY).apply { encodeSerializableValue(serializer, value) }.value)

    /**
     * Decodes plain values to a `T` with [deserializer], as the Android SDK's mapper does in calls such as
     * `DocumentSnapshot.toObject(Class)`. For Firestore, [documentReference] is the document that `@DocumentId`
     * properties receive (its id for a [String] property); it is ignored by the other products.
     */
    public fun <T> decodeFromPlainValue(deserializer: DeserializationStrategy<T>, value: Any?, documentReference: Any? = null): T = PlainDecoder(this, value, ErrorPath.EMPTY, documentReference).decodeSerializableValue(deserializer)

    /**
     * Decodes plain values to an instance of [type], found by its serializer, as the Android SDK's
     * `CustomClassMapper.convertToCustomClass(Object, Class, DocumentReference)` does; `null` stays `null`. [Any] and
     * the product's own value types are returned as they are, like `Object` and a `Timestamp` on Android.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> convertToCustomClass(value: Any?, type: KClass<T>, documentReference: Any? = null): T? {
        val value = value.asRead() ?: return null
        val serializer = when {
            type == Any::class || rules.isNativeType(type) -> ContextualSerializer(type)
            else -> serializerOf(type) ?: throw rules.mappingError("Class ${type.simpleName} is not @Serializable")
        }
        return decodeFromPlainValue(serializer as KSerializer<T>, value, documentReference)
    }

    /**
     * The serializer for [type], where [Any] (as in `Map<String, Any?>`) stands for the plain value itself, as `Object`
     * does for the Android mapper: it is decoded as is and encoded by its runtime type. The product's own value types
     * are passed through the same way, and the type arguments of a generic class are resolved by this rule too. So is a
     * type without a serializer of its own, such as the `Comparable` a map of mixed values infers, unless it is the base
     * of a polymorphic hierarchy registered in the [serializersModule].
     */
    @Suppress("UNCHECKED_CAST")
    public fun serializerFor(type: KType): KSerializer<Any?> {
        val arguments = type.arguments.map { it.type ?: typeOf<Any?>() }
        val classifier = type.classifier
        val serializer: KSerializer<*> = when {
            classifier == Any::class || classifier is KClass<*> && rules.isNativeType(classifier) -> ContextualSerializer(classifier as KClass<*>)
            classifier == Map::class || classifier == HashMap::class || classifier == LinkedHashMap::class || classifier == MutableMap::class ->
                MapSerializer(serializerFor(arguments[0]), serializerFor(arguments[1]))
            classifier == List::class || classifier == ArrayList::class || classifier == MutableList::class ->
                ListSerializer(serializerFor(arguments[0]))
            classifier == Set::class || classifier == HashSet::class || classifier == LinkedHashSet::class || classifier == MutableSet::class ->
                SetSerializer(serializerFor(arguments[0]))
            arguments.isNotEmpty() && classifier is KClass<*> -> serializerOrRuntime(classifier) { serializersModule.serializer(classifier, arguments.map(::serializerFor), false) }
            else -> serializerOrRuntime(classifier) { serializersModule.serializer(type) }
        }
        return (if (type.isMarkedNullable) (serializer as KSerializer<Any>).nullable else serializer) as KSerializer<Any?>
    }

    /**
     * The serializer [find] finds, or the runtime-typed one ([Any]'s) when it finds none or an open polymorphic one for a
     * [classifier] that is not a registered base class: an interface (JS and Wasm have no serializer for it, the JVM an
     * open polymorphic one) stands for whatever value it holds, as it does for the Android mapper.
     */
    private fun serializerOrRuntime(classifier: KClassifier?, find: () -> KSerializer<*>): KSerializer<*> {
        val found = try {
            find()
        } catch (_: SerializationException) {
            return ContextualSerializer(Any::class)
        }
        return if (found.descriptor.kind == PolymorphicKind.OPEN && classifier !in polymorphicBases) ContextualSerializer(Any::class) else found
    }

    /** The base classes of the polymorphic hierarchies registered in the [serializersModule]. */
    private val polymorphicBases: Set<KClass<*>> by lazy {
        buildSet {
            serializersModule.dumpTo(
                object : SerializersModuleCollector {
                    override fun <T : Any> contextual(kClass: KClass<T>, provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>) = Unit

                    override fun <Base : Any, Sub : Base> polymorphic(baseClass: KClass<Base>, actualClass: KClass<Sub>, actualSerializer: KSerializer<Sub>) {
                        add(baseClass)
                    }

                    override fun <Base : Any> polymorphicDefaultSerializer(baseClass: KClass<Base>, defaultSerializerProvider: (value: Base) -> SerializationStrategy<Base>?) {
                        add(baseClass)
                    }

                    override fun <Base : Any> polymorphicDefaultDeserializer(baseClass: KClass<Base>, defaultDeserializerProvider: (className: String?) -> DeserializationStrategy<Base>?) {
                        add(baseClass)
                    }
                },
            )
        }
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
     * - Enums: the entry's serial name.
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

            /**
             * The platform SDK's value for a value of one of the [nativeTypes], or a `com.google.firebase.Timestamp`, that
             * the format writes: on a platform whose SDK has its own classes for them (the JS and iOS SDKs) the SDK's
             * instance, which the SDK then takes as it is; [value] itself where the SDK takes the module's classes (the
             * Android SDK and firebase-java-sdk, whose classes they are).
             */
            public fun toSdkValue(value: Any): Any = value

            /**
             * The counterpart of [toSdkValue] for a value the platform SDK returned: the module's value for an instance of
             * one of the SDK's own classes, and any other value as it is.
             */
            public fun fromSdkValue(value: Any): Any = value
        }

        override val rules: Rules = Rules.Firestore(bindings)
    }

    /**
     * Realtime Database, following `com.google.firebase.database.core.utilities.encoding.CustomClassMapper`.
     *
     * - Numbers: [Int], [Long], [Float] and [Double], where a [Float] or [Double] with an integral value is stored as a
     *   [Long] (`1.0` is stored as `1`); other numbers and [Char]s are rejected.
     * - Collections: [List]s, and [Map]s with [String] keys; sets, other collections and arrays are rejected.
     * - Enums: the entry's serial name.
     * - Rejected values throw the exception made by [exception] (the module's `DatabaseException`), without a path.
     * - A database property without a class property is logged unless the class has `@IgnoreExtraProperties`, and fails
     *   the read if it has `@ThrowOnExtraProperties`.
     */
    public class Database(
        public val exception: (message: String) -> RuntimeException,
        serializersModule: SerializersModule = EmptySerializersModule(),
    ) : FirebaseFormat(serializersModule) {
        override val rules: Rules = Rules.Database(exception)
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

/** Encodes [value] to plain values with the serializer of its type, where [Any] stands for any plain value. */
public inline fun <reified T> FirebaseFormat.encodeToPlainValue(value: T): Any? = encodeToPlainValue(serializerFor(typeOf<T>()), value)

/** Decodes plain values to a `T` with the serializer of its type, where [Any] stands for any plain value. */
@Suppress("UNCHECKED_CAST")
public inline fun <reified T> FirebaseFormat.decodeFromPlainValue(value: Any?, documentReference: Any? = null): T = decodeFromPlainValue(serializerFor(typeOf<T>()), value, documentReference) as T
