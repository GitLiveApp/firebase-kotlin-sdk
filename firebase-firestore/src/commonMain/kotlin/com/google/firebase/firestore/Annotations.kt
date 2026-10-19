/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

/*
 * The mapping annotations of the Android SDK's firebase-firestore, as plain common code so that annotated classes
 * compile everywhere; they only take effect with the Android SDK's reflective mapping.
 */

/** Marks a property that receives the document id when a document is mapped to a custom class on Android. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
public annotation class DocumentId

/** Excludes a property from the reflective mapping on Android. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
public annotation class Exclude

/** Ignores document fields without a matching property in the reflective mapping on Android. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public annotation class IgnoreExtraProperties

/** The document field name of a property in the reflective mapping on Android. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
public annotation class PropertyName(val value: String)

/** Marks a property populated with the server timestamp in the reflective mapping on Android. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
public annotation class ServerTimestamp

/** Fails the reflective mapping on Android for document fields without a matching property. */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public annotation class ThrowOnExtraProperties
