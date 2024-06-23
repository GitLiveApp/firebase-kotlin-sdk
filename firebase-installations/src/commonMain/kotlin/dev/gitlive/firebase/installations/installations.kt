package dev.gitlive.firebase.installations

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy

/** Returns the [FirebaseInstallations] instance of the default [FirebaseApp]. */
expect val Firebase.installations: FirebaseInstallations

/** Returns the [FirebaseInstallations] instance of a given [FirebaseApp]. */
expect fun Firebase.installations(app: FirebaseApp): FirebaseInstallations

/**
 * Entry point for Firebase installations.
 *
 * The Firebase installations service:
 *  - provides a unique identifier for a Firebase installation
 *  - provides an auth token for a Firebase installation
 *  - provides a API to perform GDPR-compliant deletion of a Firebase installation.
 */
expect class FirebaseInstallations {
    /**
     * Call to delete this Firebase app installation from the Firebase backend. This call may cause
     * Firebase Cloud Messaging, Firebase Remote Config, Firebase A/B Testing, or Firebase In-App
     * Messaging to not function properly.
     */
    suspend fun delete()

    /**
     * Returns a globally unique identifier of this Firebase app installation. This is a url-safe
     * base64 string of a 128-bit integer.
     */
    suspend fun getId(): String

    /**
     * Returns a valid authentication token for the Firebase installation. Generates a new token if
     * one doesn't exist, is expired, or is about to expire.
     *
     * Should only be called if the Firebase installation is registered.
     *
     * @param forceRefresh Options to get an auth token either by force refreshing or not.
     */
    suspend fun getToken(forceRefresh: Boolean): String
}

/**
 * Exception that gets thrown when an operation on Firebase Installations fails.
 */
expect class FirebaseInstallationsException: FirebaseException

