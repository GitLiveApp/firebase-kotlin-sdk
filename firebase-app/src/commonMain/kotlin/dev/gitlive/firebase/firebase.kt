/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("CommonKt")

package dev.gitlive.firebase

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * Single access point to all firebase sdks from Kotlin.
 *
 * Acts as a target for extension methods provided by sdks.
 */
public object Firebase

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
 */
public expect class FirebaseApp {
    /** Returns the unique name of this app. */
    public val name: String

    /** Returns the specified [FirebaseOptions]. */
    public val options: FirebaseOptions

    /**
     * Deletes the [FirebaseApp] and all its data. All calls to this [FirebaseApp]
     * instance will throw once it has been called.
     *
     * A no-op if delete was called before.
     */
    public suspend fun delete()
}

/** Returns the default firebase app instance. */
public expect val Firebase.app: FirebaseApp

/** Returns a named firebase app instance. */
public expect fun Firebase.app(name: String): FirebaseApp

/** Returns all firebase app instances. */
public expect fun Firebase.apps(context: Any? = null): List<FirebaseApp>

/** Initializes and returns a FirebaseApp. */
public expect fun Firebase.initialize(context: Any? = null): FirebaseApp?

/** Initializes and returns a FirebaseApp. */
public expect fun Firebase.initialize(context: Any? = null, options: FirebaseOptions): FirebaseApp

/** Initializes and returns a FirebaseApp. */
public expect fun Firebase.initialize(context: Any? = null, options: FirebaseOptions, name: String): FirebaseApp

/** Returns options of default FirebaseApp */
@Suppress("UnusedReceiverParameter")
public val Firebase.options: FirebaseOptions
    get() = Firebase.app.options

/** Configurable Firebase options. */
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
public expect open class FirebaseException : Exception

/**
 * Exception that gets thrown when an operation on Firebase fails.
 */
public expect class FirebaseNetworkException : FirebaseException

/**
 * Exception that gets thrown when an operation on Firebase fails.
 */
public expect open class FirebaseTooManyRequestsException : FirebaseException

/**
 * Exception that gets thrown when an operation on Firebase fails.
 */
public expect open class FirebaseApiNotAvailableException : FirebaseException
