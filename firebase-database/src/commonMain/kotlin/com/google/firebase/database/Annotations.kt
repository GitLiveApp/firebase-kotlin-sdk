/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.database

/*
 * The mapping annotations of the Android SDK, as plain common code, so that classes annotated for the Android SDK's
 * reflective mapper compile everywhere. Only the Android SDK reads them: on the other platforms the natural types are
 * written and read (see DatabaseReference).
 */

/** Excludes a property from the Android SDK's mapping. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD)
public annotation class Exclude

/** Makes the Android SDK's mapper ignore properties of the data that the class does not have. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public annotation class IgnoreExtraProperties

/** Makes the Android SDK's mapper fail on properties of the data that the class does not have. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public annotation class ThrowOnExtraProperties

/** The name of a property in the database, when it differs from the member's name. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD)
public annotation class PropertyName(val value: String)
