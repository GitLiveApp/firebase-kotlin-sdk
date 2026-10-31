/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import android.content.Context

/**
 * The entry point of Firebase SDKs, mirroring `com.google.firebase.FirebaseApp` from the Firebase Android SDK.
 *
 * The members taking or returning an Android `Context` (`initializeApp`, `getApps` and `getApplicationContext`) keep
 * the SDK's signatures through the [Context] stand-in: the platform's context on Android and the JVM, a placeholder
 * that is ignored on the other platforms.
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

    /** The application context on Android and the JVM; a placeholder [Context] on the other platforms, which have none. */
    public fun getApplicationContext(): Context

    public companion object {
        /** The name of the default app. */
        public val DEFAULT_APP_NAME: String

        /** Returns the default app, which must have been initialized. */
        public fun getInstance(): FirebaseApp

        /** Returns the app with the given [name], which must have been initialized. */
        public fun getInstance(name: String): FirebaseApp

        /** Initializes the default app from the platform's default configuration, as [Firebase.initialize] does. */
        public fun initializeApp(context: Context): FirebaseApp?

        /** Initializes the default app with [options], as [Firebase.initialize] does. */
        public fun initializeApp(context: Context, options: FirebaseOptions): FirebaseApp

        /** Initializes the app called [name] with [options], as [Firebase.initialize] does. */
        public fun initializeApp(context: Context, options: FirebaseOptions, name: String): FirebaseApp

        /** The initialized apps; the [context] is used on Android and the JVM and ignored on the other platforms. */
        public fun getApps(context: Context): List<FirebaseApp>
    }
}
