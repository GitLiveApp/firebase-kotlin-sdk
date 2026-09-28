/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRUser
import com.google.android.gms.tasks.Task
import dev.gitlive.firebase.auth.TvOsMultifactorNotSupportedException
import dev.gitlive.firebase.auth.TvOsPhoneAuthNotSupportedException
import platform.Foundation.NSError

/*
 * The Firebase iOS SDK provides neither phone number verification nor multi-factor authentication on tvOS.
 */

internal actual fun FIRAuth.compatSettings(): FirebaseAuthSettings = object : FirebaseAuthSettings() {
    override fun setAppVerificationDisabledForTesting(disabled: Boolean) {}
}

internal actual fun FIRUser.updatePhoneNumberCompat(credential: PhoneAuthCredential, completion: (NSError?) -> Unit): Unit = throw TvOsPhoneAuthNotSupportedException()

internal actual fun FIRUser.compatMultiFactor(auth: FirebaseAuth): MultiFactor = object : MultiFactor() {
    override val enrolledFactors: List<MultiFactorInfo> get() = emptyList()
    override fun enroll(assertion: MultiFactorAssertion, displayName: String?): Task<Nothing?> = throw TvOsMultifactorNotSupportedException()
    override fun getSession(): Task<MultiFactorSession> = throw TvOsMultifactorNotSupportedException()
    override fun unenroll(info: MultiFactorInfo): Task<Nothing?> = throw TvOsMultifactorNotSupportedException()
    override fun unenroll(factorUid: String): Task<Nothing?> = throw TvOsMultifactorNotSupportedException()
}

internal actual fun NSError.multiFactorResolver(auth: FirebaseAuth): MultiFactorResolver? = null

internal actual fun nativePhoneCredential(verificationId: String, smsCode: String): Any = throw TvOsPhoneAuthNotSupportedException()

internal actual fun nativeVerifyPhoneNumber(options: PhoneAuthOptions): Unit = throw TvOsPhoneAuthNotSupportedException()

internal actual fun nativePhoneMultiFactorAssertion(credential: PhoneAuthCredential): Any = throw TvOsMultifactorNotSupportedException()

internal actual fun nativeTotpGenerateSecret(session: MultiFactorSession): Task<TotpSecret> = throw TvOsMultifactorNotSupportedException()

internal actual fun nativeTotpAssertionForEnrollment(secret: TotpSecret, oneTimePassword: String): Any = throw TvOsMultifactorNotSupportedException()

internal actual fun nativeTotpAssertionForSignIn(enrollmentId: String, oneTimePassword: String): Any = throw TvOsMultifactorNotSupportedException()
