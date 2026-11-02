/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

/*
 * The static members of the layer that this module's own code uses. On Android and the JVM the companion objects of the
 * header stubs do not exist at runtime, so these go through AuthStatics.java, which binds to the real SDK classes; on
 * the other platforms they are the companion members themselves.
 */

internal expect fun emailCredential(email: String, password: String): AuthCredential
internal expect fun emailLinkCredential(email: String, emailLink: String): AuthCredential
internal expect fun facebookCredential(accessToken: String): AuthCredential
internal expect fun githubCredential(token: String): AuthCredential
internal expect fun googleCredential(idToken: String?, accessToken: String?): AuthCredential
internal expect fun twitterCredential(token: String, secret: String): AuthCredential
internal expect fun oAuthProviderBuilder(providerId: String, firebaseAuth: FirebaseAuth): OAuthProvider.Builder
internal expect fun oAuthCredentialBuilder(providerId: String): OAuthProvider.CredentialBuilder
internal expect fun phoneCredential(verificationId: String, smsCode: String): PhoneAuthCredential
internal expect fun verifyPhoneNumber(options: PhoneAuthOptions)
internal expect fun actionCodeSettingsBuilder(): ActionCodeSettings.Builder
