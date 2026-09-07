/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import android.content.Context
import com.google.firebase.initialize
import com.google.firebase.FirebaseApp as CompatFirebaseApp
import com.google.firebase.FirebaseOptions as CompatFirebaseOptions

/** The underlying Firebase Android SDK app. */
public val FirebaseApp.android: CompatFirebaseApp get() = compat

// The Firebase Android SDK's own Kotlin extensions (FirebaseKt) are used for initialisation: unlike the header stubs'
// companion members they are real static methods at runtime.
public actual fun Firebase.initialize(context: Any?): FirebaseApp? = com.google.firebase.Firebase.initialize(context as Context)?.let { FirebaseApp(it) }

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions): FirebaseApp = FirebaseApp(com.google.firebase.Firebase.initialize(context as Context, options.toCompat()))

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = FirebaseApp(com.google.firebase.Firebase.initialize(context as Context, options.toCompat(), name))

public actual fun Firebase.apps(context: Any?): List<FirebaseApp> = FirebaseAppStatics.getApps(context as Context).map { FirebaseApp(it) }

internal actual suspend fun CompatFirebaseApp.deleteAwaiting(): Unit = delete()

internal actual fun FirebaseOptions.toCompat(): CompatFirebaseOptions = toCompatBuilder().build()

internal actual fun CompatFirebaseOptions.toPublic(): FirebaseOptions = toPublic(authDomain = null)
