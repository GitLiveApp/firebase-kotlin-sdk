/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.*

public val PhoneAuthProvider.ios: FIRPhoneAuthProvider get() = ios

public actual class PhoneAuthProvider(internal val ios: FIRPhoneAuthProvider) {

    public actual constructor(auth: FirebaseAuth) : this(FIRPhoneAuthProvider.providerWithAuth(auth.ios))

    public actual fun credential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(ios.credentialWithVerificationID(verificationId, smsCode))

    public actual suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential {
        val verificationId: String = ios.awaitResult { ios.verifyPhoneNumber(phoneNumber, verificationProvider.delegate, it) }
        val verificationCode = verificationProvider.getVerificationCode()
        return credential(verificationId, verificationCode)
    }
}
