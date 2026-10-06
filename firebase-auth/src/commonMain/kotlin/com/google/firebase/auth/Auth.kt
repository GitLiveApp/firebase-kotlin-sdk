/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp

/*
 * The Kotlin extensions of the Android SDK's firebase-auth (AuthKt), as plain common code: on Android and the JVM the
 * facade is a header stub that is stripped, so the SDK's own binds.
 */

/** The [FirebaseAuth] of the default [FirebaseApp]; the Android SDK's `Firebase.auth`. */
public val Firebase.auth: FirebaseAuth
    get() = FirebaseAuth.getInstance()

/** The [FirebaseAuth] of [app]. */
public fun Firebase.auth(app: FirebaseApp): FirebaseAuth = FirebaseAuth.getInstance(app)

/** [ActionCodeSettings] built with [init], as the Android SDK's `actionCodeSettings`. */
public fun actionCodeSettings(init: ActionCodeSettings.Builder.() -> Unit): ActionCodeSettings = ActionCodeSettings.newBuilder().apply(init).build()

/** An [OAuthProvider] for [providerId] built with [init], as the Android SDK's `oAuthProvider`. */
public fun oAuthProvider(providerId: String, init: OAuthProvider.Builder.() -> Unit): OAuthProvider = OAuthProvider.newBuilder(providerId).apply(init).build()

/** An [OAuthProvider] for [providerId] and [firebaseAuth] built with [init], as the Android SDK's `oAuthProvider`. */
public fun oAuthProvider(providerId: String, firebaseAuth: FirebaseAuth, init: OAuthProvider.Builder.() -> Unit): OAuthProvider = OAuthProvider.newBuilder(providerId, firebaseAuth).apply(init).build()

/** An OAuth [AuthCredential] for [providerId] built with [init], as the Android SDK's `oAuthCredential`. */
public fun oAuthCredential(providerId: String, init: OAuthProvider.CredentialBuilder.() -> Unit): AuthCredential = OAuthProvider.newCredentialBuilder(providerId).apply(init).build()

/** A [UserProfileChangeRequest] built with [init], as the Android SDK's `userProfileChangeRequest`. */
public fun userProfileChangeRequest(init: UserProfileChangeRequest.Builder.() -> Unit): UserProfileChangeRequest = UserProfileChangeRequest.Builder().apply(init).build()
