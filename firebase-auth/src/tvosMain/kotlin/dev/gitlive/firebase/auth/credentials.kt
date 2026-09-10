/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

public actual class PhoneAuthProvider() {

    public actual constructor(auth: FirebaseAuth) : this()

    public actual fun credential(
        verificationId: String,
        smsCode: String,
    ): PhoneAuthCredential = throw TvOsPhoneAuthNotSupportedException()

    public actual suspend fun verifyPhoneNumber(
        phoneNumber: String,
        verificationProvider: PhoneVerificationProvider,
    ): AuthCredential = throw TvOsPhoneAuthNotSupportedException()
}

public class TvOsPhoneAuthNotSupportedException : UnsupportedOperationException("Phone authentication is not supported on tvOS")
