/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("DEPRECATION")

package dev.gitlive.firebase

import com.google.firebase.FirebaseApp as CompatFirebaseApp
import com.google.firebase.FirebaseOptions as CompatFirebaseOptions
import dev.gitlive.firebase.externals.FirebaseApp as JsFirebaseApp

/** The underlying Firebase JS SDK app. */
public val FirebaseApp.js: JsFirebaseApp get() = compat.js

internal actual fun FirebaseOptions.toCompat(): CompatFirebaseOptions = toCompatBuilder().setAuthDomain(authDomain).build()

internal actual fun CompatFirebaseOptions.toPublic(): FirebaseOptions = toPublic(authDomain = authDomain)
