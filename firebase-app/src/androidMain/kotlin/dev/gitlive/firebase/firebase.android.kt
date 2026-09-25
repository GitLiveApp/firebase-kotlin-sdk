/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("FirebaseKt")
@file:JvmMultifileClass
@file:Suppress("DEPRECATION")

package dev.gitlive.firebase

import com.google.firebase.FirebaseApp as CompatFirebaseApp
import com.google.firebase.FirebaseOptions as CompatFirebaseOptions

/** The underlying Firebase Android SDK app. */
public val FirebaseApp.android: CompatFirebaseApp get() = compat

// The Firebase Android SDK's own Kotlin extensions (FirebaseKt) are used for initialisation: unlike the header stubs'
// companion members they are real static methods at runtime.
internal actual fun FirebaseOptions.toCompat(): CompatFirebaseOptions = toCompatBuilder().build()

internal actual fun CompatFirebaseOptions.toPublic(): FirebaseOptions = toPublic(authDomain = null)
