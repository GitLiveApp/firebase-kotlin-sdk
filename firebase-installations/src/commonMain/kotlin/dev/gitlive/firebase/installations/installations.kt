@file:JvmName("InstallationsKt")
@file:JvmMultifileClass
@file:Suppress("DEPRECATION")

package dev.gitlive.firebase.installations

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.tasks.await
import com.google.firebase.installations.FirebaseInstallations as CompatFirebaseInstallations
import com.google.firebase.installations.installations as compatInstallations

// The Android-SDK-shaped entry points are reached through the `Firebase.installations` extensions (InstallationsKt),
// which are real static methods on every platform, rather than the companion object of the header stub.

/** Message of the deprecated `dev.gitlive` members that only delegate to the `com.google.firebase` layer. */
internal const val DELEGATES_TO_ANDROID_SDK_API =
    "Only delegates to the com.google.firebase layer, which common code can use directly; see the ReplaceWith"

/** Returns the [FirebaseInstallations] instance of the default [FirebaseApp]. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.Firebase.installations", "com.google.firebase.installations.installations"))
public val Firebase.installations: FirebaseInstallations
    get() = FirebaseInstallations(com.google.firebase.Firebase.compatInstallations)

/** Returns the [FirebaseInstallations] instance of a given [FirebaseApp]. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.Firebase.installations(app.compat)", "com.google.firebase.installations.installations"))
public fun Firebase.installations(app: FirebaseApp): FirebaseInstallations = FirebaseInstallations(com.google.firebase.Firebase.compatInstallations(app.compat))

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
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.installations.FirebaseInstallations"))
public class FirebaseInstallations internal constructor(public val compat: CompatFirebaseInstallations) {
    /**
     * Call to delete this Firebase app installation from the Firebase backend. This call may cause
     * Firebase Cloud Messaging, Firebase Remote Config, Firebase A/B Testing, or Firebase In-App
     * Messaging to not function properly.
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.delete().await()", "kotlinx.coroutines.tasks.await"))
    public suspend fun delete() {
        compat.delete().await()
    }

    /**
     * Returns a globally unique identifier of this Firebase app installation. This is a url-safe
     * base64 string of a 128-bit integer.
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.getId().await()", "kotlinx.coroutines.tasks.await"))
    public suspend fun getId(): String = compat.getId().await()

    /**
     * Returns a valid authentication token for the Firebase installation. Generates a new token if
     * one doesn't exist, is expired, or is about to expire.
     *
     * Should only be called if the Firebase installation is registered.
     *
     * @param forceRefresh Options to get an auth token either by force refreshing or not.
     */
    @Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("compat.getToken(forceRefresh).await().token", "kotlinx.coroutines.tasks.await"))
    public suspend fun getToken(forceRefresh: Boolean): String = compat.getToken(forceRefresh).await().token
}

/**
 * Exception that gets thrown when an operation on Firebase Installations fails.
 */
public typealias FirebaseInstallationsException = com.google.firebase.installations.FirebaseInstallationsException
