/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth.internal

import com.google.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.externals.signOut
import kotlinx.coroutines.await

internal actual suspend fun FirebaseAuth.signOutAwaiting() {
    signOut(js).await()
}
