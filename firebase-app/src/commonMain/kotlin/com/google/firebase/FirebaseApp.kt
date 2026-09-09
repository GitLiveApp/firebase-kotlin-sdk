/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import dev.gitlive.firebase.APPLICATION_CONTEXT_ANDROID_ONLY
import dev.gitlive.firebase.GET_APPS_ANDROID_ONLY
import dev.gitlive.firebase.INITIALIZE_APP_ANDROID_ONLY

/**
 * The entry point of Firebase SDKs, mirroring `com.google.firebase.FirebaseApp` from the Firebase Android SDK.
 *
 * The members taking or returning an Android `Context` (`initializeApp`, `getApps` and `getApplicationContext`) can
 * only be called from Android code: from common code they fail to compile with a message naming the replacement
 * (`Firebase.initialize(context)` and `Firebase.getApps(context)`).
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

    /** Android only: the application context this app was initialized with. */
    @Deprecated(APPLICATION_CONTEXT_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public fun getApplicationContext(): Any

    public companion object {
        /** The name of the default app. */
        public val DEFAULT_APP_NAME: String

        /** Returns the default app, which must have been initialized. */
        public fun getInstance(): FirebaseApp

        /** Returns the app with the given [name], which must have been initialized. */
        public fun getInstance(name: String): FirebaseApp

        /** Android only: use [Firebase.initialize] from common code. */
        @Deprecated(INITIALIZE_APP_ANDROID_ONLY, ReplaceWith("Firebase.initialize(context)", "com.google.firebase.Firebase", "com.google.firebase.initialize"), DeprecationLevel.ERROR)
        public fun initializeApp(context: Any?): FirebaseApp?

        /** Android only: use [Firebase.initialize] from common code. */
        @Deprecated(INITIALIZE_APP_ANDROID_ONLY, ReplaceWith("Firebase.initialize(context, options)", "com.google.firebase.Firebase", "com.google.firebase.initialize"), DeprecationLevel.ERROR)
        public fun initializeApp(context: Any?, options: FirebaseOptions): FirebaseApp

        /** Android only: use [Firebase.initialize] from common code. */
        @Deprecated(INITIALIZE_APP_ANDROID_ONLY, ReplaceWith("Firebase.initialize(context, options, name)", "com.google.firebase.Firebase", "com.google.firebase.initialize"), DeprecationLevel.ERROR)
        public fun initializeApp(context: Any?, options: FirebaseOptions, name: String): FirebaseApp

        /** Android only: use [Firebase.getApps] from common code. */
        @Deprecated(GET_APPS_ANDROID_ONLY, ReplaceWith("Firebase.getApps(context)", "com.google.firebase.Firebase", "com.google.firebase.getApps"), DeprecationLevel.ERROR)
        public fun getApps(context: Any?): List<FirebaseApp>
    }
}
