/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth.internal

import com.google.firebase.auth.FirebaseAuth

/** Signs out, waiting for the JS SDK (which clears the current user asynchronously) to finish. */
internal expect suspend fun FirebaseAuth.signOutAwaiting()
