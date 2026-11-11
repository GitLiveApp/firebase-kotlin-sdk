/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import dev.gitlive.firebase.auth.PLAY_GAMES_ANDROID_ONLY

/*
 * Play Games sign-in exists only in the Firebase Android SDK. The classes are declared so that common code referring to them
 * fails with a message saying so, rather than with an unresolved reference; on Android the real classes are used.
 */

/** Android only: a credential from Play Games sign-in. */
@Deprecated(PLAY_GAMES_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public class PlayGamesAuthCredential private constructor()

/** Android only: creates [PlayGamesAuthCredential]s from a Play Games server auth code. */
@Deprecated(PLAY_GAMES_ANDROID_ONLY, level = DeprecationLevel.ERROR)
public class PlayGamesAuthProvider private constructor()
