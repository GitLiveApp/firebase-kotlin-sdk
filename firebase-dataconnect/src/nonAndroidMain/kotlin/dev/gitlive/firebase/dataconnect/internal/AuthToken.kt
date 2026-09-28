/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.dataconnect.internal

import com.google.firebase.FirebaseApp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.app
import dev.gitlive.firebase.auth.auth

/**
 * The ID token of the user signed in to [this] app, or `null` when nobody is or Firebase Auth is not usable; sent as
 * `X-Firebase-Auth-Token` so that connectors with an authentication level see the user, as the Android SDK's
 * auth-interop does.
 */
internal suspend fun FirebaseApp.idTokenOrNull(): String? = runCatching {
    Firebase.auth(Firebase.app(name)).currentUser?.getIdToken(false)
}.getOrNull()
