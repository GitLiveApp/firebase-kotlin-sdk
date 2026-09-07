/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import dev.gitlive.firebase.externals.deleteApp
import kotlinx.coroutines.await
import com.google.firebase.FirebaseApp as CompatFirebaseApp
import com.google.firebase.FirebaseOptions as CompatFirebaseOptions
import dev.gitlive.firebase.externals.FirebaseApp as JsFirebaseApp

/** The underlying Firebase JS SDK app. */
public val FirebaseApp.js: JsFirebaseApp get() = compat.js

internal actual suspend fun CompatFirebaseApp.deleteAwaiting() {
    deleteApp(js).await()
}

internal actual fun FirebaseOptions.toCompat(): CompatFirebaseOptions = toCompatBuilder().setAuthDomain(authDomain).build()

internal actual fun CompatFirebaseOptions.toPublic(): FirebaseOptions = toPublic(authDomain = authDomain)
