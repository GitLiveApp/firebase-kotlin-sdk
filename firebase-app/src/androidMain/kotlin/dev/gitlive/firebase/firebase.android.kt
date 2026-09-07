/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import com.google.firebase.FirebaseApp as CompatFirebaseApp
import com.google.firebase.FirebaseOptions as CompatFirebaseOptions
import dev.gitlive.firebase.android.FirebaseApp as AndroidFirebaseApp

/** The underlying (relocated) Firebase Android SDK app. */
public val FirebaseApp.android: AndroidFirebaseApp get() = compat.android

internal actual suspend fun CompatFirebaseApp.deleteAwaiting(): Unit = delete()

internal actual fun FirebaseOptions.toCompat(): CompatFirebaseOptions = toCompatBuilder().build()

internal actual fun CompatFirebaseOptions.toPublic(): FirebaseOptions = toPublic(authDomain = null)
