/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.installations

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException

/**
 * Entry point for Firebase installations, mirroring `com.google.firebase.installations.FirebaseInstallations`
 * from the Firebase Android SDK.
 *
 * Not mirrored: `clearFidCache`, `registerFidListener` (internal API).
 */
public expect class FirebaseInstallations {
    /**
     * Returns a globally unique identifier of this Firebase app installation. This is a url-safe
     * base64 string of a 128-bit integer.
     */
    public fun getId(): Task<String>

    /**
     * Returns a valid authentication token for the Firebase installation. Generates a new token if
     * one doesn't exist, is expired, or is about to expire.
     *
     * @param forceRefresh Options to get an auth token either by force refreshing or not.
     */
    public fun getToken(forceRefresh: Boolean): Task<InstallationTokenResult>

    /**
     * Call to delete this Firebase app installation from the Firebase backend. This call may cause
     * Firebase Cloud Messaging, Firebase Remote Config, Firebase A/B Testing, or Firebase In-App
     * Messaging to not function properly.
     */
    public fun delete(): Task<Nothing?>

    public companion object {
        /** Returns the [FirebaseInstallations] instance of the default [FirebaseApp]. */
        public fun getInstance(): FirebaseInstallations

        /** Returns the [FirebaseInstallations] instance of the given [app]. */
        public fun getInstance(app: FirebaseApp): FirebaseInstallations
    }
}

/** The result of [FirebaseInstallations.getToken]. */
public expect abstract class InstallationTokenResult() {
    /** The authentication token for the Firebase installation. */
    public abstract val token: String

    /** The token expiration timestamp in milliseconds since the epoch (`0` where the platform SDK does not report it). */
    public abstract val tokenExpirationTimestamp: Long
}

/** Exception that gets thrown when an operation on Firebase Installations fails. */
public expect class FirebaseInstallationsException : FirebaseException {
    public constructor(status: Status)
    public constructor(message: String, status: Status)
    public constructor(message: String, status: Status, cause: Throwable)

    /** The reason the operation failed. */
    public val status: Status

    public enum class Status {
        BAD_CONFIG,
        UNAVAILABLE,
        TOO_MANY_REQUESTS,
    }
}
