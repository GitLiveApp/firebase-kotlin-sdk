/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import dev.gitlive.firebase.auth.TO_JSON_ANDROID_ONLY
import dev.gitlive.firebase.auth.TOTP_OPEN_WITH_ACTIVITY_ANDROID_ONLY
import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.auth.TOTP_ASSERTION_CONSTRUCTOR_ANDROID_ONLY
import dev.gitlive.firebase.auth.TOTP_QR_CODE_URL_ANDROID_ONLY
import dev.gitlive.firebase.auth.TOTP_SECRET_PARAMETERS_ANDROID_ONLY

/**
 * The multi-factor enrollment of a user, mirroring `com.google.firebase.auth.MultiFactor`: enrolls a second factor from
 * a [MultiFactorAssertion] and lists or removes the enrolled ones.
 */
public expect abstract class MultiFactor() {
    /** The enrolled second factors. */
    public abstract val enrolledFactors: List<MultiFactorInfo>

    /** Enrolls the factor of [assertion] under [displayName]; requires a recent sign-in. */
    public abstract fun enroll(assertion: MultiFactorAssertion, displayName: String?): Task<Nothing?>

    /** A session for enrolling a factor, passed to the factor's verification. */
    public abstract fun getSession(): Task<MultiFactorSession>

    /** Removes the enrolled [info]. */
    public abstract fun unenroll(info: MultiFactorInfo): Task<Nothing?>

    /** Removes the enrolled factor with [factorUid]. */
    public abstract fun unenroll(factorUid: String): Task<Nothing?>
}

/** A verified second factor to enroll or sign in with, mirroring `com.google.firebase.auth.MultiFactorAssertion`. */
public expect abstract class MultiFactorAssertion() {
    /** The type of factor, e.g. [PhoneMultiFactorGenerator.FACTOR_ID]. */
    public abstract val factorId: String
}

/**
 * An enrolled second factor, mirroring `com.google.firebase.auth.MultiFactorInfo`. Its JSON form, [toJson], is Android-only
 * and declared through the error-deprecated `org.json.JSONObject` stand-in.
 */
public expect abstract class MultiFactorInfo() {
    /** Android only: the factor as JSON. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TO_JSON_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public open fun toJson(): org.json.JSONObject

    public abstract val displayName: String?

    /** When the factor was enrolled, in milliseconds since the epoch. */
    public abstract val enrollmentTimestamp: Long

    /** The type of factor, e.g. [PhoneMultiFactorGenerator.FACTOR_ID]. */
    public abstract val factorId: String
    public abstract val uid: String

    public companion object {
        public val FACTOR_ID_KEY: String
    }
}

/** An enrolled phone factor, mirroring `com.google.firebase.auth.PhoneMultiFactorInfo`. */
public expect class PhoneMultiFactorInfo(uid: String, displayName: String?, enrollmentTimestamp: Long, phoneNumber: String) : MultiFactorInfo {
    override val displayName: String?
    override val enrollmentTimestamp: Long
    override val factorId: String
    override val uid: String
    public val phoneNumber: String
}

/** An enrolled TOTP (authenticator app) factor, mirroring `com.google.firebase.auth.TotpMultiFactorInfo`. */
public expect class TotpMultiFactorInfo : MultiFactorInfo {
    override val displayName: String?
    override val enrollmentTimestamp: Long
    override val factorId: String
    override val uid: String
}

/**
 * A session for enrolling a second factor or signing in with one, mirroring `com.google.firebase.auth.MultiFactorSession`.
 */
public expect abstract class MultiFactorSession()

/**
 * Completes a sign-in that requires a second factor, mirroring `com.google.firebase.auth.MultiFactorResolver`; from
 * [FirebaseAuthMultiFactorException.resolver].
 */
public expect abstract class MultiFactorResolver() {
    public abstract val firebaseAuth: FirebaseAuth

    /** The factors the user can sign in with. */
    public abstract val hints: List<MultiFactorInfo>

    /** The session to verify one of the [hints] with. */
    public abstract val session: MultiFactorSession

    /** Completes the sign-in with the verified [assertion]. */
    public abstract fun resolveSignIn(assertion: MultiFactorAssertion): Task<AuthResult>
}

/** A verified phone factor, mirroring `com.google.firebase.auth.PhoneMultiFactorAssertion`. */
public expect class PhoneMultiFactorAssertion(credential: PhoneAuthCredential) : MultiFactorAssertion {
    override val factorId: String
}

/** Creates phone factor assertions, mirroring `com.google.firebase.auth.PhoneMultiFactorGenerator`. */
public expect class PhoneMultiFactorGenerator() {
    public companion object {
        public val FACTOR_ID: String

        /** An assertion for the verified phone [credential]. */
        public fun getAssertion(credential: PhoneAuthCredential): PhoneMultiFactorAssertion
    }
}

/** A verified TOTP factor, mirroring `com.google.firebase.auth.TotpMultiFactorAssertion`. */
public expect class TotpMultiFactorAssertion : MultiFactorAssertion {
    /** Android only: the iOS and JS SDKs only create assertions through [TotpMultiFactorGenerator]. */
    @Deprecated(TOTP_ASSERTION_CONSTRUCTOR_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public constructor(oneTimePassword: String, secret: TotpSecret, enrollmentId: String)

    override val factorId: String
}

/** Creates TOTP factor secrets and assertions, mirroring `com.google.firebase.auth.TotpMultiFactorGenerator`. */
public expect class TotpMultiFactorGenerator {
    public companion object {
        public val FACTOR_ID: String

        /** Generates a secret to enroll an authenticator app with, in [session] from [MultiFactor.getSession]. */
        public fun generateSecret(session: MultiFactorSession): Task<TotpSecret>

        /** An assertion for enrolling [secret], from the [oneTimePassword] the authenticator app shows. */
        public fun getAssertionForEnrollment(secret: TotpSecret, oneTimePassword: String): TotpMultiFactorAssertion

        /** An assertion for signing in with the enrolled factor [enrollmentId], from the [oneTimePassword] the authenticator app shows. */
        public fun getAssertionForSignIn(enrollmentId: String, oneTimePassword: String): TotpMultiFactorAssertion
    }
}

/**
 * A TOTP secret to enroll an authenticator app with, mirroring `com.google.firebase.auth.TotpSecret`.
 *
 * The secret's parameters and session are Android-only, as the iOS SDK does not expose them (see `api/android-sdk/exclusions.txt`):
 * they are declared with an error-level deprecation that says so, and Android code in `androidMain` reads them as before.
 */
public expect interface TotpSecret {
    /** The shared secret, to enter in the authenticator app by hand. */
    public val sharedSecretKey: String

    /** A URL of a QR code the authenticator app can scan, for [accountName] at [issuer]. */
    public fun generateQrCodeUrl(accountName: String, issuer: String): String

    /** Opens [qrCodeUrl] in the authenticator app. */
    public fun openInOtpApp(qrCodeUrl: String)

    /** Android only: opens [qrCodeUrl] in the authenticator app from [activity], or [fallbackUrl] when there is none. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(TOTP_OPEN_WITH_ACTIVITY_ANDROID_ONLY, ReplaceWith("openInOtpApp(qrCodeUrl)"), DeprecationLevel.ERROR)
    public fun openInOtpApp(qrCodeUrl: String, fallbackUrl: String, activity: android.app.Activity)

    /** Android only: the interval, in seconds, after which the one-time password changes. */
    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public val codeIntervalSeconds: Int

    /** Android only: the number of digits of a one-time password. */
    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public val codeLength: Int

    /** Android only: the time by which enrollment must be completed, in seconds since the epoch. */
    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public val enrollmentCompletionDeadline: Long

    /** Android only: the hashing algorithm of the one-time passwords. */
    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public val hashAlgorithm: String

    /** Android only: the enrollment session this secret belongs to. */
    @Deprecated(TOTP_SECRET_PARAMETERS_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public val sessionInfo: String

    /** Android only: a QR code URL without an account name or issuer, which the iOS SDK requires. */
    @Deprecated(TOTP_QR_CODE_URL_ANDROID_ONLY, level = DeprecationLevel.ERROR)
    public fun generateQrCodeUrl(): String
}
