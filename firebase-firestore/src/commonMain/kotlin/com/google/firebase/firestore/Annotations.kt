/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.firestore

/*
 * The mapping annotations of the Android SDK's firebase-firestore, deprecated: `FirebaseFormat.Firestore` (see
 * firestoreFormat), which maps a `@Serializable` class on every platform, Android included, reads kotlinx.serialization's
 * own `@SerialName` and `@Transient` and the format's `com.google.firebase.serialization` annotations, which
 * kotlinx.serialization records on every platform. These classes cannot be: on Android and the JVM they are header stubs
 * of the SDK's own annotations (see buildSrc utils/HeaderStubs.kt), and app code compiles against the SDK's classes,
 * which carry no `@SerialInfo`.
 */

/** The SDK's annotation; [com.google.firebase.serialization.DocumentId] is read on every platform. */
@Deprecated("FirebaseFormat reads com.google.firebase.serialization.DocumentId on every platform", ReplaceWith("DocumentId", "com.google.firebase.serialization.DocumentId"))
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public annotation class DocumentId

/** The SDK's exclusion annotation; [kotlinx.serialization.Transient] excludes a property on every platform. */
@Deprecated("FirebaseFormat reads @Transient on every platform", ReplaceWith("Transient", "kotlinx.serialization.Transient"))
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public annotation class Exclude

/** The SDK's annotation; [com.google.firebase.serialization.IgnoreExtraProperties] is read on every platform. */
@Deprecated("FirebaseFormat reads com.google.firebase.serialization.IgnoreExtraProperties on every platform", ReplaceWith("IgnoreExtraProperties", "com.google.firebase.serialization.IgnoreExtraProperties"))
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public annotation class IgnoreExtraProperties

/** The SDK's property name annotation; [kotlinx.serialization.SerialName] names a property on every platform. */
@Deprecated("FirebaseFormat reads @SerialName on every platform", ReplaceWith("SerialName(value)", "kotlinx.serialization.SerialName"))
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public annotation class PropertyName(val value: String)

/** The SDK's annotation; [com.google.firebase.serialization.ServerTimestamp] is read on every platform. */
@Deprecated("FirebaseFormat reads com.google.firebase.serialization.ServerTimestamp on every platform", ReplaceWith("ServerTimestamp", "com.google.firebase.serialization.ServerTimestamp"))
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
public annotation class ServerTimestamp

/** The SDK's annotation; [com.google.firebase.serialization.ThrowOnExtraProperties] is read on every platform. */
@Deprecated("FirebaseFormat reads com.google.firebase.serialization.ThrowOnExtraProperties on every platform", ReplaceWith("ThrowOnExtraProperties", "com.google.firebase.serialization.ThrowOnExtraProperties"))
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
public annotation class ThrowOnExtraProperties
