/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

/**
 * Single access point to all Firebase SDKs, mirroring `com.google.firebase.Firebase` from the Firebase Android SDK.
 *
 * The `com.google.firebase` packages in this library mirror the Firebase Android SDK API (including `Task` return
 * types) so that Android code compiles unchanged on every platform; the `dev.gitlive.firebase` packages offer the
 * Kotlin-first (suspending) API built on top of them. On Android and the JVM these declarations are only compiled
 * against and never shipped: the real Firebase Android SDK classes are used.
 */
public object Firebase

/** Returns the default [FirebaseApp] instance. */
public val Firebase.app: FirebaseApp
    get() = FirebaseApp.getInstance()

/** Returns the [FirebaseApp] instance with the given [name]. */
public fun Firebase.app(name: String): FirebaseApp = FirebaseApp.getInstance(name)

/** Returns the [FirebaseOptions] of the default [FirebaseApp]. */
public val Firebase.options: FirebaseOptions
    get() = Firebase.app.options
