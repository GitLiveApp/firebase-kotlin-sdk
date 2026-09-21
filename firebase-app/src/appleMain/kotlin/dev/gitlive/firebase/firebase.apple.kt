/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("DEPRECATION")

package dev.gitlive.firebase

import cocoapods.FirebaseCore.FIRApp
import com.google.firebase.FirebaseApp as CompatFirebaseApp
import com.google.firebase.FirebaseOptions as CompatFirebaseOptions

/** The underlying Firebase iOS SDK app. */
public val FirebaseApp.ios: FIRApp get() = compat.ios

internal actual fun FirebaseOptions.toCompat(): CompatFirebaseOptions = toCompatBuilder().build()

internal actual fun CompatFirebaseOptions.toPublic(): FirebaseOptions = toPublic(authDomain = null)
