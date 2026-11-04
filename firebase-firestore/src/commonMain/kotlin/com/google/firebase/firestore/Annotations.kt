/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalSerializationApi::class)

package com.google.firebase.firestore

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/*
 * The mapping annotations of the Android SDK's firebase-firestore. On Android and the JVM they are header stubs of the
 * SDK's own, which its reflective mapper reads from a class's getters and fields; on the other platforms
 * `FirebaseFormat.Firestore` reads them from a `@Serializable` class's properties, where kotlinx.serialization records
 * them, so they also target the property and carry `@SerialInfo`. A class shared across the platforms therefore
 * annotates both the getter and the property: `@get:PropertyName("stars") @PropertyName("stars") val starCount: Long`;
 * a class annotation needs no such doubling.
 */

/** Marks a property that receives the document id when a document is mapped to a custom class. */
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public expect annotation class DocumentId()

/** Excludes a property from the mapping. */
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public expect annotation class Exclude()

/** Ignores document fields without a matching property (they are logged otherwise). */
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public expect annotation class IgnoreExtraProperties()

/** The document field name of a property. */
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public expect annotation class PropertyName(val value: String)

/** Marks a `Timestamp` or `Instant` property written as the server timestamp when it is null. */
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public expect annotation class ServerTimestamp()

/** Fails the mapping for document fields without a matching property. */
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public expect annotation class ThrowOnExtraProperties()
