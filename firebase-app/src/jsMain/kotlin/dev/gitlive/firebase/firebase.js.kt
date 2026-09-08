/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import dev.gitlive.firebase.externals.deleteApp
import kotlinx.coroutines.await
import com.google.firebase.Firebase as CompatFirebase
import com.google.firebase.FirebaseApp as CompatFirebaseApp
import com.google.firebase.FirebaseOptions as CompatFirebaseOptions
import com.google.firebase.initialize as compatInitialize
import dev.gitlive.firebase.externals.FirebaseApp as JsFirebaseApp

/** The underlying Firebase JS SDK app. */
public val FirebaseApp.js: JsFirebaseApp get() = compat.js

public actual fun Firebase.initialize(context: Any?): FirebaseApp? = CompatFirebase.compatInitialize(context)?.let { FirebaseApp(it) }

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions): FirebaseApp = FirebaseApp(CompatFirebase.compatInitialize(context, options.toCompat()))

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = FirebaseApp(CompatFirebase.compatInitialize(context, options.toCompat(), name))

public actual fun Firebase.apps(context: Any?): List<FirebaseApp> = CompatFirebaseApp.getApps().map { FirebaseApp(it) }

internal actual suspend fun CompatFirebaseApp.deleteAwaiting() {
    deleteApp(js).await()
}

internal actual fun FirebaseOptions.toCompat(): CompatFirebaseOptions = toCompatBuilder().setAuthDomain(authDomain).build()

internal actual fun CompatFirebaseOptions.toPublic(): FirebaseOptions = toPublic(authDomain = authDomain)
