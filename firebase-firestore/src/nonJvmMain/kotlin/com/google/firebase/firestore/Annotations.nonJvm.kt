/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalSerializationApi::class)

package com.google.firebase.firestore

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

// Recorded by kotlinx.serialization when applied to a property or a class, for FirebaseFormat.Firestore (see firestoreFormat).

@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public actual annotation class DocumentId

@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public actual annotation class Exclude

@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public actual annotation class IgnoreExtraProperties

@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public actual annotation class PropertyName(actual val value: String)

@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public actual annotation class ServerTimestamp

@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public actual annotation class ThrowOnExtraProperties
