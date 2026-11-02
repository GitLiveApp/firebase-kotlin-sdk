/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth.internal

import com.google.firebase.auth.FirebaseAuth

internal actual suspend fun FirebaseAuth.signOutAwaiting(): Unit = signOut()
