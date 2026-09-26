/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("android")
@file:JvmMultifileClass

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import com.google.firebase.auth.auth as compatAuth

// The Android-SDK-shaped entry points are reached through the `Firebase.auth` extensions (AuthKt), which are real static
// methods on every platform, rather than the companion object of the header stub.

/** Returns the [FirebaseAuth] instance of the default [FirebaseApp]. */
public val Firebase.auth: FirebaseAuth
    get() = FirebaseAuth(com.google.firebase.Firebase.compatAuth)

/** Returns the [FirebaseAuth] instance of a given [FirebaseApp]. */
public fun Firebase.auth(app: FirebaseApp): FirebaseAuth = FirebaseAuth(com.google.firebase.Firebase.compatAuth(app.compat))

/** The error code of the platform SDK, e.g. `ERROR_INVALID_EMAIL`. */
public val FirebaseAuthException.code: String? get() = errorCode
