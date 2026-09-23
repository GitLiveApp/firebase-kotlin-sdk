/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import dev.gitlive.firebase.FROM_RESOURCE_ANDROID_ONLY

/**
 * Configurable Firebase options, mirroring `com.google.firebase.FirebaseOptions` from the Firebase Android SDK.
 * `fromResource` takes an Android `Context` and can only be called from Android code: from common code it fails to
 * compile with a message naming the replacement, [Firebase.fromResource].
 */
public expect class FirebaseOptions {
    /** API key used for authenticating requests from your app. */
    public val apiKey: String

    /** The Google App ID that is used to uniquely identify an instance of an app. */
    public val applicationId: String

    /** The database root URL, e.g. `http://abc-xyz-123.firebaseio.com`. */
    public val databaseUrl: String?

    /** The Project Number from the Google Developer's console, used to configure Google Cloud Messaging. */
    public val gcmSenderId: String?

    /** The Google Cloud project ID. */
    public val projectId: String?

    /** The Google Cloud Storage bucket name. */
    public val storageBucket: String?

    /** The tracking ID for Google Analytics. */
    public val gaTrackingId: String?

    /** Builder for constructing [FirebaseOptions]. */
    public class Builder() {
        public constructor(options: FirebaseOptions)
        public fun setApiKey(apiKey: String): Builder
        public fun setApplicationId(applicationId: String): Builder
        public fun setDatabaseUrl(databaseUrl: String?): Builder
        public fun setGcmSenderId(gcmSenderId: String?): Builder
        public fun setProjectId(projectId: String?): Builder
        public fun setStorageBucket(storageBucket: String?): Builder
        public fun setGaTrackingId(gaTrackingId: String?): Builder
        public fun build(): FirebaseOptions
    }

    public companion object {
        /** Android only: use [Firebase.fromResource] from common code. */
        @Deprecated(FROM_RESOURCE_ANDROID_ONLY, ReplaceWith("Firebase.fromResource(context)", "com.google.firebase.Firebase", "com.google.firebase.fromResource"), DeprecationLevel.ERROR)
        public fun fromResource(context: Any?): FirebaseOptions?
    }
}
