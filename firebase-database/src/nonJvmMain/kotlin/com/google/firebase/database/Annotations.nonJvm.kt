/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:OptIn(ExperimentalSerializationApi::class)

package com.google.firebase.database

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

// Recorded by kotlinx.serialization when applied to a property or a class, for FirebaseFormat.Database (see databaseFormat).

@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
public actual annotation class Exclude

@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public actual annotation class IgnoreExtraProperties

@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public actual annotation class ThrowOnExtraProperties

@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
public actual annotation class PropertyName(actual val value: String)
