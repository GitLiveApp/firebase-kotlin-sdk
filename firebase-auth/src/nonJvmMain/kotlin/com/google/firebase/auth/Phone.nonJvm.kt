/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import com.google.firebase.FirebaseException

public actual class PhoneAuthProvider internal constructor(internal val firebaseAuth: FirebaseAuth) {

    /** Resends the code of the verification [options] with the same options. */
    public actual class ForceResendingToken internal constructor(internal val options: PhoneAuthOptions)

    public actual abstract class OnVerificationStateChangedCallbacks actual constructor() {
        public actual abstract fun onVerificationCompleted(credential: PhoneAuthCredential)
        public actual abstract fun onVerificationFailed(exception: FirebaseException)
        public actual open fun onCodeSent(verificationId: String, token: ForceResendingToken) {}
        public actual open fun onCodeAutoRetrievalTimeOut(verificationId: String) {}
    }

    public actual companion object {
        public actual val PROVIDER_ID: String = "phone"
        public actual val PHONE_SIGN_IN_METHOD: String = "phone"

        public actual fun getCredential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(nativePhoneCredential(verificationId, smsCode), smsCode)

        public actual fun verifyPhoneNumber(options: PhoneAuthOptions) {
            nativeVerifyPhoneNumber(options)
        }

        @Deprecated("Use PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions)")
        public actual fun getInstance(): PhoneAuthProvider = PhoneAuthProvider(FirebaseAuth.getInstance())

        @Deprecated("Use PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions)")
        public actual fun getInstance(firebaseAuth: FirebaseAuth): PhoneAuthProvider = PhoneAuthProvider(firebaseAuth)
    }
}

/**
 * A pure Kotlin value.
 *
 * @property firebaseAuth The instance the number is verified with.
 * @property phoneNumber The number to verify, or null for a multi-factor sign-in with [multiFactorHint].
 * @property callbacks The callbacks of the verification.
 * @property multiFactorHint The enrolled phone factor to verify, for a multi-factor sign-in.
 * @property multiFactorSession The session of a multi-factor enrollment or sign-in.
 * @property requireSmsValidation Whether the user must enter the code even when the SDK could verify the number itself.
 * @property verificationContext On JS the `ApplicationVerifier` (reCAPTCHA) of the verification, on iOS the `FIRAuthUIDelegate` presenting it.
 */
public actual class PhoneAuthOptions internal constructor(
    public val firebaseAuth: FirebaseAuth,
    public val phoneNumber: String?,
    public val callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks,
    public val multiFactorHint: PhoneMultiFactorInfo?,
    public val multiFactorSession: MultiFactorSession?,
    public val requireSmsValidation: Boolean,
    internal val verificationContext: Any?,
) {
    public actual class Builder actual constructor(private val firebaseAuth: FirebaseAuth) {
        private var phoneNumber: String? = null
        private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
        private var multiFactorHint: PhoneMultiFactorInfo? = null
        private var multiFactorSession: MultiFactorSession? = null
        private var requireSmsValidation = false
        internal var verificationContext: Any? = null

        public actual fun setPhoneNumber(phoneNumber: String): Builder = apply { this.phoneNumber = phoneNumber }
        public actual fun setCallbacks(callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks): Builder = apply { this.callbacks = callbacks }
        public actual fun setForceResendingToken(token: PhoneAuthProvider.ForceResendingToken): Builder = apply {
            phoneNumber = phoneNumber ?: token.options.phoneNumber
            multiFactorHint = multiFactorHint ?: token.options.multiFactorHint
            multiFactorSession = multiFactorSession ?: token.options.multiFactorSession
            verificationContext = verificationContext ?: token.options.verificationContext
        }
        public actual fun setMultiFactorHint(hint: PhoneMultiFactorInfo): Builder = apply { multiFactorHint = hint }
        public actual fun setMultiFactorSession(session: MultiFactorSession): Builder = apply { multiFactorSession = session }
        public actual fun requireSmsValidation(requireSmsValidation: Boolean): Builder = apply { this.requireSmsValidation = requireSmsValidation }
        public actual fun build(): PhoneAuthOptions {
            require(phoneNumber != null || multiFactorHint != null) { "You must specify a phone number or a multi-factor hint." }
            return PhoneAuthOptions(firebaseAuth, phoneNumber, requireNotNull(callbacks) { "You must specify the callbacks." }, multiFactorHint, multiFactorSession, requireSmsValidation, verificationContext)
        }
    }

    public actual companion object {
        public actual fun newBuilder(): Builder = Builder(FirebaseAuth.getInstance())

        public actual fun newBuilder(firebaseAuth: FirebaseAuth): Builder = Builder(firebaseAuth)
    }
}

/** Reports the verification id the platform SDK sent a code for to the callbacks of [options]. */
internal fun PhoneAuthOptions.codeSent(verificationId: String) {
    callbacks.onCodeSent(verificationId, PhoneAuthProvider.ForceResendingToken(this))
}
