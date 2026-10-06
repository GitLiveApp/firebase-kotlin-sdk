/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

internal actual fun emailCredential(email: String, password: String): AuthCredential = EmailAuthProvider.getCredential(email, password)

internal actual fun emailLinkCredential(email: String, emailLink: String): AuthCredential = EmailAuthProvider.getCredentialWithLink(email, emailLink)

internal actual fun facebookCredential(accessToken: String): AuthCredential = FacebookAuthProvider.getCredential(accessToken)

internal actual fun githubCredential(token: String): AuthCredential = GithubAuthProvider.getCredential(token)

internal actual fun googleCredential(idToken: String?, accessToken: String?): AuthCredential = GoogleAuthProvider.getCredential(idToken, accessToken)

internal actual fun twitterCredential(token: String, secret: String): AuthCredential = TwitterAuthProvider.getCredential(token, secret)

internal actual fun oAuthProviderBuilder(providerId: String, firebaseAuth: FirebaseAuth): OAuthProvider.Builder = OAuthProvider.newBuilder(providerId, firebaseAuth)

internal actual fun oAuthCredentialBuilder(providerId: String): OAuthProvider.CredentialBuilder = OAuthProvider.newCredentialBuilder(providerId)

internal actual fun phoneCredential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, smsCode)

internal actual fun verifyPhoneNumber(options: PhoneAuthOptions) = PhoneAuthProvider.verifyPhoneNumber(options)

internal actual fun actionCodeSettingsBuilder(): ActionCodeSettings.Builder = ActionCodeSettings.newBuilder()
