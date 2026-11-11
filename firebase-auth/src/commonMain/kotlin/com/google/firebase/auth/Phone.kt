/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import dev.gitlive.firebase.auth.PHONE_ACTIVITY_ANDROID_ONLY
import dev.gitlive.firebase.auth.PHONE_TIMEOUT_ANDROID_ONLY
import dev.gitlive.firebase.auth.VERIFY_PHONE_NUMBER_DEPRECATED
import com.google.firebase.FirebaseException

/**
 * Phone number sign-in, mirroring `com.google.firebase.auth.PhoneAuthProvider`: [verifyPhoneNumber] sends an SMS code
 * and reports to the [PhoneAuthOptions] callbacks; [getCredential] turns the verification id and the code the user
 * entered into a credential.
 *
 * On Android the SDK can also verify the number without user input (SMS auto-retrieval); the other platforms always
 * report [OnVerificationStateChangedCallbacks.onCodeSent].
 */
public expect class PhoneAuthProvider {
    /** Deprecated in the SDK and Android-only: verifies [phoneNumber] with the instance API; use [verifyPhoneNumber] with [PhoneAuthOptions]. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(VERIFY_PHONE_NUMBER_DEPRECATED, level = DeprecationLevel.ERROR)
    public fun verifyPhoneNumber(phoneNumber: String, timeout: Long, unit: java.util.concurrent.TimeUnit, activity: android.app.Activity, callbacks: OnVerificationStateChangedCallbacks)

    /** Deprecated in the SDK and Android-only: resends the code of [forceResendingToken]; use [verifyPhoneNumber] with [PhoneAuthOptions]. */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(VERIFY_PHONE_NUMBER_DEPRECATED, level = DeprecationLevel.ERROR)
    public fun verifyPhoneNumber(phoneNumber: String, timeout: Long, unit: java.util.concurrent.TimeUnit, activity: android.app.Activity, callbacks: OnVerificationStateChangedCallbacks, forceResendingToken: ForceResendingToken)

    /** A token of a sent code; passing it to [PhoneAuthOptions.Builder.setForceResendingToken] sends a new one. */
    public class ForceResendingToken

    /** The callbacks of [verifyPhoneNumber]. */
    public abstract class OnVerificationStateChangedCallbacks {
        public constructor()

        /** The SDK verified the number itself; sign in with [credential]. */
        public abstract fun onVerificationCompleted(credential: PhoneAuthCredential)

        /** Verification failed, e.g. with a [FirebaseAuthInvalidCredentialsException] for a malformed number. */
        public abstract fun onVerificationFailed(exception: FirebaseException)

        /** An SMS code was sent; ask the user for it and call [getCredential] with [verificationId]. */
        public open fun onCodeSent(verificationId: String, token: ForceResendingToken)

        /** Auto-retrieval timed out without a code; the user must enter it. */
        public open fun onCodeAutoRetrievalTimeOut(verificationId: String)
    }

    public companion object {
        public val PROVIDER_ID: String
        public val PHONE_SIGN_IN_METHOD: String

        /** A credential for the [smsCode] the user entered for [verificationId]. */
        public fun getCredential(verificationId: String, smsCode: String): PhoneAuthCredential

        /** Starts the verification described by [options]. */
        public fun verifyPhoneNumber(options: PhoneAuthOptions)

        @Deprecated("Use PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions)")
        public fun getInstance(): PhoneAuthProvider

        @Deprecated("Use PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions)")
        public fun getInstance(firebaseAuth: FirebaseAuth): PhoneAuthProvider
    }
}

/**
 * The options of a phone number verification, mirroring `com.google.firebase.auth.PhoneAuthOptions`; built with
 * [Builder]. The `Activity` the verification is attached to and the auto-retrieval timeout are Android-only, declared through
 * the error-deprecated stand-ins.
 */
public expect class PhoneAuthOptions {
    public class Builder(firebaseAuth: FirebaseAuth) {
        /** The number to verify, in E.164 format. */
        public fun setPhoneNumber(phoneNumber: String): Builder

        public fun setCallbacks(callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks): Builder

        /** Resends the code of an earlier verification. */
        public fun setForceResendingToken(token: PhoneAuthProvider.ForceResendingToken): Builder

        /** For multi-factor sign-in: the enrolled phone factor to verify, from [MultiFactorResolver.hints]. */
        public fun setMultiFactorHint(hint: PhoneMultiFactorInfo): Builder

        /** For multi-factor enrollment or sign-in: the session from [MultiFactor.getSession] or [MultiFactorResolver.session]. */
        public fun setMultiFactorSession(session: MultiFactorSession): Builder

        /** Whether the user must enter the code even when the SDK could verify the number itself. */
        public fun requireSmsValidation(requireSmsValidation: Boolean): Builder

        /** Android only: the activity the verification (and its reCAPTCHA fallback) is attached to. */
        @Suppress("DEPRECATION_ERROR")
        @Deprecated(PHONE_ACTIVITY_ANDROID_ONLY, level = DeprecationLevel.ERROR)
        public fun setActivity(activity: android.app.Activity): Builder

        /** Android only: how long to wait for SMS auto-retrieval before [PhoneAuthProvider.OnVerificationStateChangedCallbacks.onCodeAutoRetrievalTimeOut]. */
        @Suppress("DEPRECATION_ERROR")
        @Deprecated(PHONE_TIMEOUT_ANDROID_ONLY, level = DeprecationLevel.ERROR)
        public fun setTimeout(timeout: Long?, unit: java.util.concurrent.TimeUnit): Builder

        public fun build(): PhoneAuthOptions
    }

    public companion object {
        /** A builder for the default [FirebaseAuth]. */
        public fun newBuilder(): Builder

        /** A builder for [firebaseAuth]. */
        public fun newBuilder(firebaseAuth: FirebaseAuth): Builder
    }
}
