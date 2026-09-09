/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import cocoapods.FirebaseCore.FIRApp
import kotlinx.coroutines.CompletableDeferred
import com.google.firebase.Firebase as CompatFirebase
import com.google.firebase.FirebaseApp as CompatFirebaseApp
import com.google.firebase.FirebaseOptions as CompatFirebaseOptions
import com.google.firebase.getApps as compatGetApps
import com.google.firebase.initialize as compatInitialize

/** The underlying Firebase iOS SDK app. */
public val FirebaseApp.ios: FIRApp get() = compat.ios

public actual fun Firebase.initialize(context: Any?): FirebaseApp? = CompatFirebase.compatInitialize(context)?.let { FirebaseApp(it) }

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions): FirebaseApp = FirebaseApp(CompatFirebase.compatInitialize(context, options.toCompat()))

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = FirebaseApp(CompatFirebase.compatInitialize(context, options.toCompat(), name))

public actual fun Firebase.apps(context: Any?): List<FirebaseApp> = CompatFirebase.compatGetApps(context).map { FirebaseApp(it) }

internal actual suspend fun CompatFirebaseApp.deleteAwaiting() {
    val deleted = CompletableDeferred<Unit>()
    ios.deleteApp { deleted.complete(Unit) }
    deleted.await()
}

internal actual fun FirebaseOptions.toCompat(): CompatFirebaseOptions = toCompatBuilder().build()

internal actual fun CompatFirebaseOptions.toPublic(): FirebaseOptions = toPublic(authDomain = null)
