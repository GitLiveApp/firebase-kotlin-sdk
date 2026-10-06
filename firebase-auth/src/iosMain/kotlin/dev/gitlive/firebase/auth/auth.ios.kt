/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.FIRMultiFactor
import cocoapods.FirebaseAuth.FIRMultiFactorAssertion
import cocoapods.FirebaseAuth.FIRMultiFactorInfo
import cocoapods.FirebaseAuth.FIRMultiFactorResolver
import cocoapods.FirebaseAuth.FIRMultiFactorSession
import cocoapods.FirebaseAuth.FIRPhoneAuthProvider
import com.google.firebase.auth.MultiFactorImpl
import com.google.firebase.auth.MultiFactorResolverImpl
import com.google.firebase.auth.MultiFactorSessionImpl
import com.google.firebase.auth.phoneCredential
import com.google.firebase.auth.setUIDelegate
import kotlinx.coroutines.CompletableDeferred
import com.google.firebase.auth.PhoneAuthOptions as CompatPhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider as CompatPhoneAuthProvider
import com.google.firebase.auth.verifyPhoneNumber as startPhoneVerification

public val MultiFactor.ios: FIRMultiFactor get() = (compat as MultiFactorImpl).ios

public val MultiFactorInfo.ios: FIRMultiFactorInfo get() = compat.native as FIRMultiFactorInfo

public val MultiFactorAssertion.ios: FIRMultiFactorAssertion get() = compat.native as FIRMultiFactorAssertion

public val MultiFactorSession.ios: FIRMultiFactorSession get() = (compat as MultiFactorSessionImpl).native as FIRMultiFactorSession

public val MultiFactorResolver.ios: FIRMultiFactorResolver get() = (compat as MultiFactorResolverImpl).ios

public val PhoneAuthProvider.ios: FIRPhoneAuthProvider get() = FIRPhoneAuthProvider.providerWithAuth(auth.ios)

public actual class PhoneAuthProvider actual constructor(internal val auth: FirebaseAuth) {

    public actual fun credential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(phoneCredential(verificationId, smsCode))

    public actual suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential {
        val sentVerificationId = CompletableDeferred<String>()
        val options = CompatPhoneAuthOptions.Builder(auth.compat)
            .setPhoneNumber(phoneNumber)
            .setUIDelegate(verificationProvider.delegate)
            .setCallbacks(
                object : CompatPhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onCodeSent(verificationId: String, token: CompatPhoneAuthProvider.ForceResendingToken) {
                        sentVerificationId.complete(verificationId)
                    }

                    override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {}

                    override fun onVerificationFailed(exception: com.google.firebase.FirebaseException) {
                        sentVerificationId.completeExceptionally(exception)
                    }
                },
            )
            .build()
        startPhoneVerification(options)
        return credential(sentVerificationId.await(), verificationProvider.getVerificationCode())
    }
}
