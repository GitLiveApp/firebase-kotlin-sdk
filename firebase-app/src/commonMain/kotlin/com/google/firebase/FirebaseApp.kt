/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

/**
 * The entry point of Firebase SDKs, mirroring `com.google.firebase.FirebaseApp` from the Firebase Android SDK.
 *
 * `context` parameters accept the Android `Context` on Android and are ignored on other platforms.
 * Not mirrored: `getApplicationContext`, `setAutomaticResourceManagementEnabled`.
 */
public expect class FirebaseApp {
    /** The unique name of this app. */
    public val name: String

    /** The options this app was configured with. */
    public val options: FirebaseOptions

    /** Deletes this app and frees its resources. A no-op if the app was already deleted. */
    public fun delete()

    public companion object {
        /** The name of the default app. */
        public val DEFAULT_APP_NAME: String

        /** Returns the default app, which must have been initialized. */
        public fun getInstance(): FirebaseApp

        /** Returns the app with the given [name], which must have been initialized. */
        public fun getInstance(name: String): FirebaseApp

        /** Initializes the default app from the platform's configuration file, or returns null if there is none. */
        public fun initializeApp(context: Any?): FirebaseApp?

        /** Initializes the default app with the given [options]. */
        public fun initializeApp(context: Any?, options: FirebaseOptions): FirebaseApp

        /** Initializes an app with the given [options] and [name]. */
        public fun initializeApp(context: Any?, options: FirebaseOptions, name: String): FirebaseApp

        /** Returns all initialized apps. */
        public fun getApps(context: Any?): List<FirebaseApp>
    }
}
