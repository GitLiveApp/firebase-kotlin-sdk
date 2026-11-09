/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

// Deprecation messages of the com.google.firebase.database members that map custom classes with the Android SDK's
// reflection; kept out of the stripped package, where a constant would be verified against the SDK's own facade.

internal const val REFLECTIVE_WRITE = "setValue maps a custom class with the Android SDK's reflection, which only exists on Android and the JVM; setData maps a @Serializable class, or a plain value, with kotlinx.serialization on every platform"
internal const val REFLECTIVE_UPDATE = "updateChildren maps custom classes in the values with the Android SDK's reflection, which only exists on Android and the JVM; updateData maps @Serializable classes and plain values with kotlinx.serialization on every platform"
internal const val REFLECTIVE_READ = "Maps the class with the Android SDK's reflection, which only exists on Android and the JVM; getValue<T>() in com.google.firebase.serialization maps a @Serializable class with kotlinx.serialization on every platform"
