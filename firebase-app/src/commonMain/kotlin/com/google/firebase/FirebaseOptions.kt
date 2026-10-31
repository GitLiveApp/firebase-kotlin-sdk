/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase

import android.content.Context

/**
 * Configurable Firebase options, mirroring `com.google.firebase.FirebaseOptions` from the Firebase Android SDK.
 * `fromResource` takes the [Context] stand-in: the platform's context on Android and the JVM, a placeholder that is
 * ignored on the other platforms.
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

    /**
     * The tracking ID for Google Analytics. Always `null` on Apple platforms, where firebase-ios-sdk 12 removed
     * `FIROptions.trackingID`.
     */
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
        /**
         * The options of the platform's default configuration: on Android the resources the Google Services Gradle
         * plugin generates from `google-services.json`, on Apple platforms `GoogleService-Info.plist` (the [context] is
         * ignored); null when there is none, as on JS and wasmJs, which have no default configuration.
         */
        public fun fromResource(context: Context): FirebaseOptions?
    }
}
