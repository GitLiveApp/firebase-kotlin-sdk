/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

// Deprecation messages of the com.google.firebase.auth members that only Android can provide; kept out of the stripped
// package, where a constant would be verified against the SDK's own facade.

internal const val TOTP_SECRET_PARAMETERS_ANDROID_ONLY =
    "The TOTP secret's parameters (codeLength, codeIntervalSeconds, hashAlgorithm, enrollmentCompletionDeadline, sessionInfo) are Android-only: " +
        "the Firebase iOS SDK does not expose them; sharedSecretKey, generateQrCodeUrl(accountName, issuer) and openInOtpApp(qrCodeUrl) " +
        "are available on every platform, and a call that needs the parameters belongs in androidMain"
internal const val TOTP_QR_CODE_URL_ANDROID_ONLY =
    "generateQrCodeUrl() without arguments is Android-only: the Firebase iOS SDK requires the account name and issuer; " +
        "use generateQrCodeUrl(accountName, issuer)"
internal const val TOTP_ASSERTION_CONSTRUCTOR_ANDROID_ONLY =
    "Constructing a TotpMultiFactorAssertion is Android-only: the Firebase iOS and JS SDKs only create assertions through " +
        "TotpMultiFactorGenerator; use getAssertionForEnrollment(secret, oneTimePassword) or getAssertionForSignIn(enrollmentId, oneTimePassword)"
internal const val PLAY_GAMES_ANDROID_ONLY =
    "Play Games sign-in is Android-only: the Firebase iOS and JS SDKs have no Play Games provider; a call belongs in androidMain"
