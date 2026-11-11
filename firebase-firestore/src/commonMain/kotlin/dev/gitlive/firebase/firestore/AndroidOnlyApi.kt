/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

// Deprecation messages of the com.google.firebase.firestore members that map custom classes with the Android SDK's
// reflection; kept out of the stripped package, where a constant would be verified against the SDK's own facade.

internal const val REFLECTIVE_SET = "set maps a custom class with the Android SDK's reflection, which only exists on Android and the JVM; setData maps a @Serializable class, or a plain map, with kotlinx.serialization on every platform"
internal const val REFLECTIVE_UPDATE = "update maps custom classes in the values with the Android SDK's reflection, which only exists on Android and the JVM; updateData maps @Serializable classes and plain values with kotlinx.serialization on every platform"
internal const val REFLECTIVE_ADD = "add maps a custom class with the Android SDK's reflection, which only exists on Android and the JVM; addData maps a @Serializable class, or a plain map, with kotlinx.serialization on every platform"
internal const val REFLECTIVE_READ = "Maps the class with the Android SDK's reflection, which only exists on Android and the JVM; the reified toObject<T>(), toObjects<T>(), getField<T>() and dataObjects<T>() in com.google.firebase.serialization map a @Serializable class with kotlinx.serialization on every platform"

// The java.util.Date accessors, which only Android and the JVM can call (java.util.Date is an error-deprecated stand-in in common code).
internal const val GET_DATE_ANDROID_ONLY = "getDate returns a java.util.Date, which is JVM-only; use getTimestamp, and toKotlinInstant() on the Timestamp"

// The bundle loaders from JVM streams, which only Android and the JVM can call (java.io.InputStream and java.nio.ByteBuffer are error-deprecated stand-ins in common code).
internal const val LOAD_BUNDLE_STREAM_ANDROID_ONLY = "loadBundle(InputStream) is JVM-only; use loadBundle(RawSource) (kotlinx-io), on the JVM with bundleData.asSource()"
internal const val LOAD_BUNDLE_BUFFER_ANDROID_ONLY = "loadBundle(ByteBuffer) is JVM-only; use loadBundle(ByteArray) with the buffer's bytes"

// The pipeline API's java.util.Date constant, which only Android and the JVM can call.
internal const val CONSTANT_OF_DATE_ANDROID_ONLY = "Expression.constant(java.util.Date) is JVM-only; use constant(Timestamp)"

// The constructors of the pipeline classes that the Android SDK builds through its API only: internal here, and
// error-deprecated so that the header stub verification does not look for them on the SDK's classes.
internal const val INTERNAL_CONSTRUCTOR = "Internal constructor; the class is built through the pipeline API"

// The Activity and Executor overloads of the listeners, which only Android and the JVM can call (android.app.Activity and
// java.util.concurrent.Executor are error-deprecated stand-ins in common code).
internal const val LISTENER_EXECUTOR_JVM_ONLY = "The Executor overloads of the snapshot listeners are JVM-only; the multiplatform listener is called on the thread that delivers the snapshot: use the overload without an Executor and dispatch from the listener"
internal const val LISTENER_ACTIVITY_ANDROID_ONLY = "The Activity overloads of the snapshot listeners are Android-only, where the listener is removed when the Activity stops; use the overload without an Activity and remove the ListenerRegistration yourself"
internal const val PROGRESS_LISTENER_EXECUTOR_JVM_ONLY = "addOnProgressListener(Executor, OnProgressListener) is JVM-only; the multiplatform listener is called on the thread that reports the progress: use addOnProgressListener(listener) and dispatch from the listener"
internal const val PROGRESS_LISTENER_ACTIVITY_ANDROID_ONLY = "addOnProgressListener(Activity, OnProgressListener) is Android-only, where the listener is dropped when the Activity stops; use addOnProgressListener(listener)"
internal const val LISTEN_OPTIONS_EXECUTOR_JVM_ONLY = "SnapshotListenOptions.executor is JVM-only; the multiplatform listener is called on the thread that delivers the snapshot, so dispatch from the listener"
internal const val LISTEN_OPTIONS_ACTIVITY_ANDROID_ONLY = "SnapshotListenOptions.activity is Android-only, where the listener is removed when the Activity stops; remove the ListenerRegistration yourself"

// The pipeline API's protobuf-typed members, which only Android and the JVM can call (com.google.firestore.v1.Value is an
// error-deprecated stand-in in common code).
internal const val PROTO_VALUE_ANDROID_ONLY = "com.google.firestore.v1.Value is the Android SDK's protobuf wire type and only exists on Android and the JVM; pass the option as a String, Boolean, Long, Double, Field or RawOptions"
internal const val DIRECTION_PROTO_ANDROID_ONLY = "Ordering.Direction.proto is the Android SDK's protobuf wire value and only exists on Android and the JVM"

// The Android SDK's local pipeline evaluation helpers, declared so that common code referring to them fails with a message.
internal const val PIPELINE_EVALUATION_ANDROID_ONLY = "The com.google.firebase.firestore.pipeline.evaluation helpers are internal to the Android SDK's local evaluation of pipelines and only exist on Android and the JVM"

// Deprecated in the Android SDK and not implemented off Android.
internal const val INDEX_CONFIGURATION_DEPRECATED = "Deprecated in the Firebase Android SDK and not available off Android; let the SDK create cache indexes with persistentCacheIndexManager?.enableIndexAutoCreation()"

// Deprecated in the Android SDK in favour of the cache settings, and mapped onto them here.
internal const val PERSISTENCE_SETTINGS_DEPRECATED = "Deprecated in the Firebase Android SDK; use setLocalCacheSettings with PersistentCacheSettings or MemoryCacheSettings"
internal const val CACHE_API_MIXED = "New cache config API setLocalCacheSettings() is already used."
internal const val DEPRECATED_CACHE_API_MIXED = "Deprecated cache config API setPersistenceEnabled()/setCacheSizeBytes() is already used."
