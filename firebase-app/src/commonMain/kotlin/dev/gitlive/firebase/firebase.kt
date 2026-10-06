/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("FirebaseKt")
@file:JvmMultifileClass
@file:Suppress("DEPRECATION")

package dev.gitlive.firebase

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

import com.google.firebase.app
import com.google.firebase.deleteApp
import com.google.firebase.getApps
import com.google.firebase.initialize as compatInitialize
import kotlinx.coroutines.tasks.await
import com.google.firebase.FirebaseApp as CompatFirebaseApp
import com.google.firebase.FirebaseOptions as CompatFirebaseOptions

/**
 * Single access point to all firebase sdks from Kotlin.
 *
 * Acts as a target for extension methods provided by sdks.
 */
public object Firebase

/**
 * Message of the deprecated `dev.gitlive` members that only delegate to the `com.google.firebase` layer, which common
 * code can use directly; each names its counterpart in a `ReplaceWith`.
 */
internal const val DELEGATES_TO_ANDROID_SDK_API =
    "Only delegates to the com.google.firebase layer, which common code can use directly; see the ReplaceWith"

/**
 * The entry point of Firebase SDKs. It holds common configuration and state for Firebase APIs. Most
 * applications don't need to directly interact with FirebaseApp.
 *
 * For a vast majority of apps, FirebaseInitProvider will handle the initialization of
 * Firebase for the default project that it's configured to work with, via the data contained in the
 * app's `google-services.json` file. This `ContentProvider`
 * is merged into the app's manifest by default when building with Gradle,
 * and it runs automatically at app launch. No additional lines of code are needed in this
 * case.
 *
 * Any `FirebaseApp` initialization must occur only in the main process of the app.
 * Use of Firebase in processes other than the main process is not supported and will likely cause
 * problems related to resource contention.
 *
 * @property compat The Android-SDK-shaped [com.google.firebase.FirebaseApp] this app wraps.
 */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.FirebaseApp"))
public class FirebaseApp internal constructor(public val compat: CompatFirebaseApp) {
    /** Returns the unique name of this app. */
    public val name: String get() = compat.name

    /** Returns the specified [FirebaseOptions]. */
    public val options: FirebaseOptions get() = compat.options.toPublic()

    /**
     * Deletes the [FirebaseApp] and all its data. All calls to this [FirebaseApp]
     * instance will throw once it has been called.
     *
     * A no-op if delete was called before.
     */
    public suspend fun delete() {
        compat.deleteApp().await()
    }

    override fun equals(other: Any?): Boolean = other is FirebaseApp && other.compat == compat

    override fun hashCode(): Int = compat.hashCode()

    override fun toString(): String = "FirebaseApp(name=${compat.name})"
}

/** Returns the default firebase app instance. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.Firebase.app", "com.google.firebase.app"))
public val Firebase.app: FirebaseApp
    get() = FirebaseApp(com.google.firebase.Firebase.app)

/** Returns a named firebase app instance. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.Firebase.app(name)", "com.google.firebase.app"))
public fun Firebase.app(name: String): FirebaseApp = FirebaseApp(com.google.firebase.Firebase.app(name))

/** Returns all firebase app instances. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.Firebase.getApps(context)", "com.google.firebase.getApps"))
public fun Firebase.apps(context: Any? = null): List<FirebaseApp> = com.google.firebase.Firebase.getApps(context).map { FirebaseApp(it) }

/** Initializes and returns a FirebaseApp. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.Firebase.initialize(context)", "com.google.firebase.initialize"))
public fun Firebase.initialize(context: Any? = null): FirebaseApp? = com.google.firebase.Firebase.compatInitialize(context)?.let { FirebaseApp(it) }

/** Initializes and returns a FirebaseApp. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.Firebase.initialize(context, options)", "com.google.firebase.initialize"))
public fun Firebase.initialize(context: Any? = null, options: FirebaseOptions): FirebaseApp = FirebaseApp(com.google.firebase.Firebase.compatInitialize(context, options.toCompat()))

/** Initializes and returns a FirebaseApp. */
@Deprecated(DELEGATES_TO_ANDROID_SDK_API, ReplaceWith("com.google.firebase.Firebase.initialize(context, options, name)", "com.google.firebase.initialize"))
public fun Firebase.initialize(context: Any? = null, options: FirebaseOptions, name: String): FirebaseApp = FirebaseApp(com.google.firebase.Firebase.compatInitialize(context, options.toCompat(), name))

/** Configurable Firebase options; the `com.google.firebase.FirebaseOptions(...)` factory takes the same named values and returns the Android-SDK-shaped ones. */
@Deprecated(
    DELEGATES_TO_ANDROID_SDK_API,
    ReplaceWith("com.google.firebase.FirebaseOptions(applicationId, apiKey, databaseUrl, gaTrackingId, storageBucket, projectId, gcmSenderId, authDomain)", "com.google.firebase.FirebaseOptions"),
)
public data class FirebaseOptions(
    /** The Google App ID that is used to uniquely identify an instance of an app. */
    val applicationId: String,

    /**
     * API key used for authenticating requests from your app, e.g.
     * AIzaSyDdVgKwhZl0sTTTLZ7iTmt1r3N2cJLnaDk, used to identify your app to Google servers.
     */
    val apiKey: String,

    /** The database root URL, e.g. http://abc-xyz-123.firebaseio.com. */
    val databaseUrl: String? = null,

    /**
     * The tracking ID for Google Analytics, e.g. UA-12345678-1, used to configure Google Analytics.
     */
    val gaTrackingId: String? = null,

    /** The Google Cloud Storage bucket name, e.g. abc-xyz-123.storage.firebase.com. */
    val storageBucket: String? = null,

    /** The Google Cloud project ID, e.g. my-project-1234 */
    val projectId: String? = null,

    /**
     * The Project Number from the Google Developer's console, for example 012345678901, used to
     * configure Google Cloud Messaging.
     */
    val gcmSenderId: String? = null,

    /** The auth domain. */
    val authDomain: String? = null,
)

/**
 * Exception that gets thrown when an operation on Firebase fails.
 */
public typealias FirebaseException = com.google.firebase.FirebaseException

/**
 * Exception that gets thrown when an operation on Firebase fails.
 */
public typealias FirebaseNetworkException = com.google.firebase.FirebaseNetworkException

/**
 * Exception that gets thrown when an operation on Firebase fails.
 */
public typealias FirebaseTooManyRequestsException = com.google.firebase.FirebaseTooManyRequestsException

/**
 * Exception that gets thrown when an operation on Firebase fails.
 */
public typealias FirebaseApiNotAvailableException = com.google.firebase.FirebaseApiNotAvailableException

/** Converts to the Android-SDK-shaped options (platform specific because JS also carries [FirebaseOptions.authDomain]). */
internal expect fun FirebaseOptions.toCompat(): CompatFirebaseOptions

internal expect fun CompatFirebaseOptions.toPublic(): FirebaseOptions

internal fun FirebaseOptions.toCompatBuilder(): CompatFirebaseOptions.Builder = compatOptionsBuilder(applicationId, apiKey, databaseUrl, gaTrackingId, storageBucket, projectId, gcmSenderId)

/** The Android-SDK-shaped builder for the given values; shipped, since the `com.google.firebase.FirebaseOptions(...)` factory uses it. */
internal fun compatOptionsBuilder(
    applicationId: String,
    apiKey: String,
    databaseUrl: String?,
    gaTrackingId: String?,
    storageBucket: String?,
    projectId: String?,
    gcmSenderId: String?,
): CompatFirebaseOptions.Builder = CompatFirebaseOptions.Builder()
    .setApplicationId(applicationId)
    .setApiKey(apiKey)
    .setDatabaseUrl(databaseUrl)
    .setGaTrackingId(gaTrackingId)
    .setStorageBucket(storageBucket)
    .setProjectId(projectId)
    .setGcmSenderId(gcmSenderId)

internal fun CompatFirebaseOptions.toPublic(authDomain: String?): FirebaseOptions = FirebaseOptions(
    applicationId = applicationId,
    apiKey = apiKey,
    databaseUrl = databaseUrl,
    gaTrackingId = gaTrackingId,
    storageBucket = storageBucket,
    projectId = projectId,
    gcmSenderId = gcmSenderId,
    authDomain = authDomain,
)
