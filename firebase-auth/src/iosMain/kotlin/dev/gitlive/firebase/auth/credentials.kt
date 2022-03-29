/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.*

class PhoneAuthCredential(override val ios: FIRPhoneAuthCredential) : AuthCredential(ios)

class PhoneAuthProvider(val ios: FIRPhoneAuthProvider) {

    constructor(auth: FirebaseAuth) : this(FIRPhoneAuthProvider.providerWithAuth(auth.ios))

    fun credential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(ios.credentialWithVerificationID(verificationId, smsCode))

    suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential {
        val verificationId: String = ios.awaitResult { ios.verifyPhoneNumber(phoneNumber, verificationProvider.delegate, it) }
        val verificationCode = verificationProvider.getVerificationCode()
        return credential(verificationId, verificationCode)
    }
}

interface PhoneVerificationProvider {
    val delegate: FIRAuthUIDelegateProtocol
    suspend fun getVerificationCode(): String
}
