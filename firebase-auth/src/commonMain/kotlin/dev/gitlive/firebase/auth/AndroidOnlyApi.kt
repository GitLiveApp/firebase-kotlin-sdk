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

// The Activity-bound flows and other Android-only members, declared through the error-deprecated android.app.Activity,
// java.util.concurrent.TimeUnit and org.json.JSONObject stand-ins so that common code gets these messages.
internal const val ACTIVITY_PROVIDER_FLOW_ANDROID_ONLY =
    "startActivityForSignInWithProvider, startActivityForLinkWithProvider and startActivityForReauthenticateWithProvider start a web flow from an " +
        "android.app.Activity and are Android-only; use signInWithCredential, linkWithCredential or reauthenticateAndRetrieveData with the provider's " +
        "credential, or call them from androidMain"
internal const val PENDING_AUTH_RESULT_ANDROID_ONLY =
    "pendingAuthResult is the result of an Activity-based provider flow interrupted by the app being recreated, and is Android-only"
internal const val FORCE_RECAPTCHA_ANDROID_ONLY =
    "forceRecaptchaFlowForTesting forces the Android SDK's reCAPTCHA verification and is Android-only; setAppVerificationDisabledForTesting is available on " +
        "every platform"
internal const val AUTO_RETRIEVED_SMS_ANDROID_ONLY =
    "setAutoRetrievedSmsCodeForPhoneNumber fakes the Android SDK's SMS auto-retrieval and is Android-only; off Android the user always enters the code"
internal const val PHONE_ACTIVITY_ANDROID_ONLY =
    "PhoneAuthOptions.Builder.setActivity attaches the verification to an android.app.Activity and is Android-only; on iOS and JS the " +
        "dev.gitlive.firebase.auth layer attaches the platform's verifier"
internal const val PHONE_TIMEOUT_ANDROID_ONLY =
    "PhoneAuthOptions.Builder.setTimeout sets the Android SDK's SMS auto-retrieval timeout and is Android-only; the other platforms have no auto-retrieval"
internal const val VERIFY_PHONE_NUMBER_DEPRECATED =
    "Deprecated in the Firebase Android SDK and Android-only (it takes a java.util.concurrent.TimeUnit and an android.app.Activity); " +
        "use PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions)"
internal const val TO_JSON_ANDROID_ONLY =
    "toJson returns an org.json.JSONObject and is Android-only; the Firebase iOS and JS SDKs have no JSON form of a MultiFactorInfo"
internal const val TOTP_OPEN_WITH_ACTIVITY_ANDROID_ONLY =
    "openInOtpApp(qrCodeUrl, fallbackUrl, activity) is Android-only; use openInOtpApp(qrCodeUrl)"
