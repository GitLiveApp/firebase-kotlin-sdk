package dev.gitlive.firebase.installations

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.tasks.await
import com.google.firebase.installations.FirebaseInstallations as CompatFirebaseInstallations

/** Returns the [FirebaseInstallations] instance of the default [FirebaseApp]. */
public val Firebase.installations: FirebaseInstallations
    get() = FirebaseInstallations(CompatFirebaseInstallations.getInstance())

/** Returns the [FirebaseInstallations] instance of a given [FirebaseApp]. */
public fun Firebase.installations(app: FirebaseApp): FirebaseInstallations = FirebaseInstallations(CompatFirebaseInstallations.getInstance(app.compat))

/**
 * Entry point for Firebase installations.
 *
 * The Firebase installations service:
 *  - provides a unique identifier for a Firebase installation
 *  - provides an auth token for a Firebase installation
 *  - provides a API to perform GDPR-compliant deletion of a Firebase installation.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.installations.FirebaseInstallations] this wraps.
 */
public class FirebaseInstallations internal constructor(public val compat: CompatFirebaseInstallations) {
    /**
     * Call to delete this Firebase app installation from the Firebase backend. This call may cause
     * Firebase Cloud Messaging, Firebase Remote Config, Firebase A/B Testing, or Firebase In-App
     * Messaging to not function properly.
     */
    public suspend fun delete() {
        compat.delete().await()
    }

    /**
     * Returns a globally unique identifier of this Firebase app installation. This is a url-safe
     * base64 string of a 128-bit integer.
     */
    public suspend fun getId(): String = compat.getId().await()

    /**
     * Returns a valid authentication token for the Firebase installation. Generates a new token if
     * one doesn't exist, is expired, or is about to expire.
     *
     * Should only be called if the Firebase installation is registered.
     *
     * @param forceRefresh Options to get an auth token either by force refreshing or not.
     */
    public suspend fun getToken(forceRefresh: Boolean): String = compat.getToken(forceRefresh).await().token
}

/**
 * Exception that gets thrown when an operation on Firebase Installations fails.
 */
public typealias FirebaseInstallationsException = com.google.firebase.installations.FirebaseInstallationsException
