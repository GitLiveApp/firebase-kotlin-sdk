/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.messaging

/** Android unit tests have no Android runtime (the SDK's RemoteMessage needs a Bundle), so the tests that need it are skipped there. */
expect annotation class IgnoreForAndroidUnitTest()
