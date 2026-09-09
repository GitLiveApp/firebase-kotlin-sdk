/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

/**
 * Initializes the default [FirebaseApp], mirroring `Firebase.initialize(Context)` from the Firebase Android SDK with
 * the `Context` widened to `Any?` so that common code can pass the platform context through.
 *
 * On Android [context] must be an `android.content.Context`; Android code that passes a `Context` keeps binding to the
 * SDK's own `Firebase.initialize(Context)`, this overload is only chosen for an `Any?` argument. On Apple platforms and
 * JS the context is ignored. Without [options] the app is configured from `google-services.json` resources on Android
 * and `GoogleService-Info.plist` on Apple platforms; JS always needs options and throws.
 */
public expect fun Firebase.initialize(context: Any? = null): FirebaseApp?

/** Initializes the default [FirebaseApp] with [options]; see [initialize]. */
public expect fun Firebase.initialize(context: Any? = null, options: FirebaseOptions): FirebaseApp

/** Initializes the [FirebaseApp] called [name] with [options]; see [initialize]. */
public expect fun Firebase.initialize(context: Any? = null, options: FirebaseOptions, name: String): FirebaseApp

/**
 * The initialized apps, mirroring `FirebaseApp.getApps(Context)` from the Firebase Android SDK as a top-level function
 * with the `Context` widened to `Any?` so that common code can call it (the static member itself cannot be given an
 * `Any?` overload). On Android [context] must be an `android.content.Context`; on Apple platforms and JS it is ignored.
 */
public expect fun Firebase.getApps(context: Any? = null): List<FirebaseApp>
