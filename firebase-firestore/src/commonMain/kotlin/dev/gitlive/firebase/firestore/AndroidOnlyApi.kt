/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

// Deprecation messages of the com.google.firebase.firestore members that map custom classes with the Android SDK's
// reflection; kept out of the stripped package, where a constant would be verified against the SDK's own facade.

internal const val REFLECTIVE_SET = "set maps a custom class with the Android SDK's reflection on Android; setData maps it through FirebaseFormat on every platform, and plain maps work through it too"
internal const val REFLECTIVE_UPDATE = "update(Map) maps custom classes with the Android SDK's reflection on Android; updateData maps them through FirebaseFormat on every platform, and plain maps work through it too"
internal const val REFLECTIVE_ADD = "add maps a custom class with the Android SDK's reflection on Android; addData maps it through FirebaseFormat on every platform, and plain maps work through it too"
internal const val REFLECTIVE_READ = "Maps a custom class with the Android SDK's reflection on Android; the reified toObject<T>(), toObjects<T>(), getField<T>() and dataObjects<T>() in com.google.firebase.serialization map it through FirebaseFormat on every platform"

// The java.util.Date accessors, which only Android and the JVM can call (java.util.Date is an error-deprecated stand-in in common code).
internal const val GET_DATE_ANDROID_ONLY = "getDate returns a java.util.Date, which is JVM-only; use getTimestamp, and toKotlinInstant() on the Timestamp"

// The bundle loaders from JVM streams, which only Android and the JVM can call (java.io.InputStream and java.nio.ByteBuffer are error-deprecated stand-ins in common code).
internal const val LOAD_BUNDLE_STREAM_ANDROID_ONLY = "loadBundle(InputStream) is JVM-only; use loadBundle(RawSource) (kotlinx-io), on the JVM with bundleData.asSource()"
internal const val LOAD_BUNDLE_BUFFER_ANDROID_ONLY = "loadBundle(ByteBuffer) is JVM-only; use loadBundle(ByteArray) with the buffer's bytes"
