/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalSerializationApi::class)

package com.google.firebase.database

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/*
 * The mapping annotations of the Android SDK. On Android and the JVM they are header stubs of the SDK's own, which its
 * reflective mapper reads from a class's getters and fields; on the other platforms `FirebaseFormat.Database` reads them
 * from a `@Serializable` class's properties, where kotlinx.serialization records them, so they also target the property
 * and carry `@SerialInfo`. A class shared across the platforms therefore annotates both the getter and the property:
 * `@get:PropertyName("stars") @PropertyName("stars") val starCount: Long`; a class annotation needs no such doubling.
 */

/** Excludes a property from the mapping. */
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
public expect annotation class Exclude()

/** Makes the mapper ignore properties of the data that the class does not have (they are logged otherwise). */
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public expect annotation class IgnoreExtraProperties()

/** Makes the mapper fail on properties of the data that the class does not have. */
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public expect annotation class ThrowOnExtraProperties()

/** The name of a property in the database, when it differs from the member's name. */
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
public expect annotation class PropertyName(val value: String)
