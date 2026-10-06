/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("AuthInternalsKt")

package com.google.firebase.auth

// Shipped (see keepClasses in the build file): the SDK's static members this module's own code needs are reached through
// AuthStatics.java, which javac compiles after the header stubs are stripped, so it binds to the real classes.

internal actual fun emailCredential(email: String, password: String): AuthCredential = AuthStatics.emailCredential(email, password)

internal actual fun emailLinkCredential(email: String, emailLink: String): AuthCredential = AuthStatics.emailLinkCredential(email, emailLink)

internal actual fun facebookCredential(accessToken: String): AuthCredential = AuthStatics.facebookCredential(accessToken)

internal actual fun githubCredential(token: String): AuthCredential = AuthStatics.githubCredential(token)

internal actual fun googleCredential(idToken: String?, accessToken: String?): AuthCredential = AuthStatics.googleCredential(idToken, accessToken)

internal actual fun twitterCredential(token: String, secret: String): AuthCredential = AuthStatics.twitterCredential(token, secret)

internal actual fun oAuthProviderBuilder(providerId: String, firebaseAuth: FirebaseAuth): OAuthProvider.Builder = AuthStatics.oAuthProviderBuilder(providerId, firebaseAuth)

internal actual fun oAuthCredentialBuilder(providerId: String): OAuthProvider.CredentialBuilder = AuthStatics.oAuthCredentialBuilder(providerId)

internal actual fun phoneCredential(verificationId: String, smsCode: String): PhoneAuthCredential = AuthStatics.phoneCredential(verificationId, smsCode)

internal actual fun verifyPhoneNumber(options: PhoneAuthOptions) = AuthStatics.verifyPhoneNumber(options)

internal actual fun actionCodeSettingsBuilder(): ActionCodeSettings.Builder = AuthStatics.actionCodeSettingsBuilder()
