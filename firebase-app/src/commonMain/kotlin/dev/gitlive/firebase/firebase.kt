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
 * <p>Acts as a target for extension methods provided by sdks.
 */
object Firebase

expect class FirebaseApp {
    val name: String
    val options: CommonFirebaseOptions
}

/** Returns the default firebase app instance. */
expect val Firebase.app: FirebaseApp

/** Returns a named firebase app instance. */
expect fun Firebase.app(name: String): FirebaseApp

/** Returns all firebase app instances. */
expect fun Firebase.apps(context: Any? = null): List<FirebaseApp>

/** Initializes and returns a FirebaseApp. */
expect fun Firebase.initialize(context: Any? = null): FirebaseApp?

/** Initializes and returns a FirebaseApp. */
expect fun Firebase.initialize(context: Any? = null, options: CommonFirebaseOptions): FirebaseApp

/** Initializes and returns a FirebaseApp. */
expect fun Firebase.initialize(context: Any? = null, options: CommonFirebaseOptions, name: String): FirebaseApp

/** Returns options of default FirebaseApp */
val Firebase.options: CommonFirebaseOptions
    get() = Firebase.app.options

open class CommonFirebaseOptions

data class FirebaseOptions(
    val applicationId: String,
    val apiKey: String,
    val databaseUrl: String? = null,
    val gaTrackingId: String? = null,
    val storageBucket: String? = null,
    val projectId: String? = null,
    val gcmSenderId: String? = null,
    val authDomain: String? = null
): CommonFirebaseOptions()

data class OldAdminFirebaseOptions(
    val serviceFileName: String
): CommonFirebaseOptions()

data class AdminFirebaseOptions(
    val credentialFile: String,
    val databaseUrl: String? = null,
    val projectId: String? = null,
    val serviceAccountId: String? = null,
    val connectTimeout: Int? = null,
    val readTimeout: Int? = null
): CommonFirebaseOptions()

expect open class FirebaseException : Exception

expect class FirebaseNetworkException : FirebaseException

expect open class FirebaseTooManyRequestsException : FirebaseException

expect open class FirebaseApiNotAvailableException : FirebaseException

