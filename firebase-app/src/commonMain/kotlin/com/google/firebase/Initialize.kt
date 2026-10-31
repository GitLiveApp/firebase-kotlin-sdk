/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import android.content.Context

/**
 * Initializes the default [FirebaseApp], mirroring `Firebase.initialize(Context)` from the Firebase Android SDK; the
 * same as [FirebaseApp.initializeApp].
 *
 * The [context] is the app's on Android and firebase-java-sdk's `Application` on the JVM; Apple platforms, JS and wasmJs
 * have none, so platform code passes a placeholder [Context] there, which is ignored. Without [options] the app is
 * configured from the `google-services.json` resources on Android (null when there are none) and from
 * `GoogleService-Info.plist` on Apple platforms; JS and wasmJs always need options and throw.
 */
public expect fun Firebase.initialize(context: Context): FirebaseApp?

/** Initializes the default [FirebaseApp] with [options]; see [initialize]. */
public expect fun Firebase.initialize(context: Context, options: FirebaseOptions): FirebaseApp

/** Initializes the [FirebaseApp] called [name] with [options]; see [initialize]. */
public expect fun Firebase.initialize(context: Context, options: FirebaseOptions, name: String): FirebaseApp
