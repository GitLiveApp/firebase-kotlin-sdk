/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

// Deprecation messages of the com.google.firebase.database members that map custom classes with the Android SDK's
// reflection; kept out of the stripped package, where a constant would be verified against the SDK's own facade.

internal const val REFLECTIVE_WRITE = "setValue maps a custom class with the Android SDK's reflection on Android; setData maps it through FirebaseFormat on every platform, and plain values work through it too"
internal const val REFLECTIVE_UPDATE = "updateChildren maps custom classes with the Android SDK's reflection on Android; updateData maps them through FirebaseFormat on every platform, and plain maps work through it too"
internal const val REFLECTIVE_READ = "Maps a custom class with the Android SDK's reflection on Android; com.google.firebase.serialization.getValue<T>() maps it through FirebaseFormat on every platform"
