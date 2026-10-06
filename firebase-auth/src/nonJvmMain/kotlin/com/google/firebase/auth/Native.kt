/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import com.google.android.gms.tasks.Task

/*
 * The static entry points of the platform SDKs behind the layer's pure Kotlin values: credentials, phone verification,
 * multi-factor assertions and action code links. The iOS SDK only provides phone and multi-factor authentication on
 * iOS, so the tvOS actuals of those fail.
 */

internal expect fun nativeEmailCredential(email: String, password: String): Any
internal expect fun nativeEmailLinkCredential(email: String, emailLink: String): Any
internal expect fun nativeFacebookCredential(accessToken: String): Any
internal expect fun nativeGithubCredential(token: String): Any
internal expect fun nativeGoogleCredential(idToken: String?, accessToken: String?): Any
internal expect fun nativeTwitterCredential(token: String, secret: String): Any
internal expect fun nativeOAuthCredential(providerId: String, idToken: String?, accessToken: String?, rawNonce: String?): Any
internal expect fun nativePhoneCredential(verificationId: String, smsCode: String): Any
internal expect fun nativeVerifyPhoneNumber(options: PhoneAuthOptions)
internal expect fun nativePhoneMultiFactorAssertion(credential: PhoneAuthCredential): Any
internal expect fun nativeTotpGenerateSecret(session: MultiFactorSession): Task<TotpSecret>
internal expect fun nativeTotpAssertionForEnrollment(secret: TotpSecret, oneTimePassword: String): Any
internal expect fun nativeTotpAssertionForSignIn(enrollmentId: String, oneTimePassword: String): Any
internal expect fun parseActionCodeUrl(link: String): ActionCodeUrl?
