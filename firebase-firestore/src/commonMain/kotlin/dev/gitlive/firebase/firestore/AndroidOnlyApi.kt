/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

// Deprecation messages of the com.google.firebase.firestore members that map custom classes with the Android SDK's
// reflection; kept out of the stripped package, where a constant would be verified against the SDK's own facade.

internal const val REFLECTIVE_SET = "set maps a custom class with the Android SDK's reflection, which only exists on Android; setData maps a @Serializable class, or a plain map, with kotlinx.serialization on every platform"
internal const val REFLECTIVE_UPDATE = "update maps custom classes in the values with the Android SDK's reflection, which only exists on Android; updateData maps @Serializable classes and plain values with kotlinx.serialization on every platform"
internal const val REFLECTIVE_ADD = "add maps a custom class with the Android SDK's reflection, which only exists on Android; addData maps a @Serializable class, or a plain map, with kotlinx.serialization on every platform"
internal const val REFLECTIVE_READ = "Maps the class with the Android SDK's reflection, which only exists on Android; the reified toObject<T>(), toObjects<T>(), getField<T>() and dataObjects<T>() in com.google.firebase.serialization map a @Serializable class with kotlinx.serialization on every platform"

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
