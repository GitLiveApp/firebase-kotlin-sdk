/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

/**
 * Single access point to all Firebase SDKs, mirroring `com.google.firebase.Firebase` from the Firebase Android SDK.
 *
 * The `com.google.firebase` packages in this library mirror the Firebase Android SDK API (including `Task` return
 * types) so that Android code compiles unchanged on every platform; the `dev.gitlive.firebase` packages offer the
 * Kotlin-first (suspending) API built on top of them.
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

/** Initializes and returns the default [FirebaseApp] from the platform's configuration file, if any. */
public fun Firebase.initialize(context: Any?): FirebaseApp? = FirebaseApp.initializeApp(context)

/** Initializes and returns the default [FirebaseApp] with the given [options]. */
public fun Firebase.initialize(context: Any?, options: FirebaseOptions): FirebaseApp = FirebaseApp.initializeApp(context, options)

/** Initializes and returns a [FirebaseApp] with the given [options] and [name]. */
public fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = FirebaseApp.initializeApp(context, options, name)
