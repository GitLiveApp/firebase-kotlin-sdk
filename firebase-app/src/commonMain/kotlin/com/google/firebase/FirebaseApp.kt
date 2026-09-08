/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

/**
 * The entry point of Firebase SDKs, mirroring `com.google.firebase.FirebaseApp` from the Firebase Android SDK.
 *
 * Not mirrored, because they take an Android `Context`: `initializeApp`, `getApps` and `getApplicationContext`
 * (use `Firebase.initialize(context)` or `dev.gitlive.firebase.Firebase.initialize` / `apps` from common code).
 */
public expect class FirebaseApp {
    /** The unique name of this app. */
    public val name: String

    /** The options this app was configured with. */
    public val options: FirebaseOptions

    /** Deletes this app and frees its resources. A no-op if the app was already deleted. */
    public fun delete()

    /**
     * On Android, lets the SDK release resources while the app is in the background and reacquire them when it
     * returns to the foreground. The other platforms have no equivalent, so this is a no-op there.
     */
    public fun setAutomaticResourceManagementEnabled(enabled: Boolean)

    public companion object {
        /** The name of the default app. */
        public val DEFAULT_APP_NAME: String

        /** Returns the default app, which must have been initialized. */
        public fun getInstance(): FirebaseApp

        /** Returns the app with the given [name], which must have been initialized. */
        public fun getInstance(name: String): FirebaseApp
    }
}
