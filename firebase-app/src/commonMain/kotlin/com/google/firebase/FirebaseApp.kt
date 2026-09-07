/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

/**
 * The entry point of Firebase SDKs, mirroring `com.google.firebase.FirebaseApp` from the Firebase Android SDK.
 *
 * Not mirrored, because they take an Android `Context`: `initializeApp`, `getApps`, `getApplicationContext`
 * (use `dev.gitlive.firebase.Firebase.initialize` / `apps` from common code), and `setAutomaticResourceManagementEnabled`.
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
    }
}
