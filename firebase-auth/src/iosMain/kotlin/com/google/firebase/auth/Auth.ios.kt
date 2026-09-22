/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRAuthUIDelegateProtocol
import cocoapods.FirebaseAuth.FIRMultiFactor
import cocoapods.FirebaseAuth.FIRMultiFactorAssertion
import cocoapods.FirebaseAuth.FIRMultiFactorInfo
import cocoapods.FirebaseAuth.FIRMultiFactorResolver
import cocoapods.FirebaseAuth.FIRMultiFactorSession
import cocoapods.FirebaseAuth.FIRPhoneAuthCredential
import cocoapods.FirebaseAuth.FIRPhoneAuthProvider
import cocoapods.FirebaseAuth.FIRPhoneMultiFactorGenerator
import cocoapods.FirebaseAuth.FIRPhoneMultiFactorInfo
import cocoapods.FirebaseAuth.FIRTOTPMultiFactorGenerator
import cocoapods.FirebaseAuth.FIRTOTPSecret
import cocoapods.FirebaseAuth.FIRUser
import com.google.android.gms.tasks.Task
import platform.Foundation.NSError

/*
 * Phone number verification and multi-factor authentication: the Firebase iOS SDK only provides them on iOS.
 */

internal actual fun FIRAuth.compatSettings(): FirebaseAuthSettings = object : FirebaseAuthSettings() {
    override fun setAppVerificationDisabledForTesting(disabled: Boolean) {
        settings()?.setAppVerificationDisabledForTesting(disabled)
    }
}

internal actual fun FIRUser.updatePhoneNumberCompat(credential: PhoneAuthCredential, completion: (NSError?) -> Unit) {
    updatePhoneNumberCredential(credential.ios as FIRPhoneAuthCredential, completion)
}

internal actual fun FIRUser.compatMultiFactor(auth: FirebaseAuth): MultiFactor = MultiFactorImpl(multiFactor(), auth)

internal actual fun NSError.multiFactorResolver(auth: FirebaseAuth): MultiFactorResolver? = (userInfo["FIRAuthErrorUserInfoMultiFactorResolverKey"] as? FIRMultiFactorResolver)?.let { MultiFactorResolverImpl(it, auth) }

internal class MultiFactorImpl(val ios: FIRMultiFactor, private val auth: FirebaseAuth) : MultiFactor() {
    override val enrolledFactors: List<MultiFactorInfo> get() = ios.enrolledFactors().mapNotNull { (it as? FIRMultiFactorInfo)?.toCompat() }

    override fun enroll(assertion: MultiFactorAssertion, displayName: String?): Task<Nothing?> = write(auth) { ios.enrollWithAssertion(assertion.native as FIRMultiFactorAssertion, displayName, it) }

    override fun getSession(): Task<MultiFactorSession> = task(auth) { completion ->
        ios.getSessionWithCompletion { session, error -> completion(session?.let { MultiFactorSessionImpl(it) }, error) }
    }

    override fun unenroll(info: MultiFactorInfo): Task<Nothing?> = write(auth) { completion ->
        (info.native as? FIRMultiFactorInfo)?.let { ios.unenrollWithInfo(it, completion) } ?: ios.unenrollWithFactorUID(info.uid, completion)
    }

    override fun unenroll(factorUid: String): Task<Nothing?> = write(auth) { ios.unenrollWithFactorUID(factorUid, it) }
}

internal class MultiFactorResolverImpl(val ios: FIRMultiFactorResolver, override val firebaseAuth: FirebaseAuth) : MultiFactorResolver() {
    override val hints: List<MultiFactorInfo> get() = ios.hints().mapNotNull { (it as? FIRMultiFactorInfo)?.toCompat() }
    override val session: MultiFactorSession get() = MultiFactorSessionImpl(ios.session())

    override fun resolveSignIn(assertion: MultiFactorAssertion): Task<AuthResult> = task(firebaseAuth) { completion ->
        ios.resolveSignInWithAssertion(assertion.native as FIRMultiFactorAssertion) { result, error -> completion(result?.toCompat(firebaseAuth), error) }
    }
}

internal class TotpSecretImpl(val ios: FIRTOTPSecret) : TotpSecret {
    override val sharedSecretKey: String get() = ios.sharedSecretKey()

    override fun generateQrCodeUrl(accountName: String, issuer: String): String = ios.generateQRCodeURLWithAccountName(accountName, issuer)

    override fun openInOtpApp(qrCodeUrl: String) {
        ios.openInOTPAppWithQRCodeURL(qrCodeUrl)
    }
}

internal fun FIRMultiFactorInfo.toCompat(): MultiFactorInfo = when (this) {
    is FIRPhoneMultiFactorInfo -> PhoneMultiFactorInfo(UID(), displayName(), enrollmentDate().toTimestamp(), phoneNumber(), this)
    else -> TotpMultiFactorInfo(UID(), displayName(), enrollmentDate().toTimestamp(), this)
}

internal actual fun nativePhoneCredential(verificationId: String, smsCode: String): Any = FIRPhoneAuthProvider.provider().credentialWithVerificationID(verificationId, smsCode)

internal actual fun nativeVerifyPhoneNumber(options: PhoneAuthOptions) {
    val provider = FIRPhoneAuthProvider.providerWithAuth(options.firebaseAuth.ios)
    val delegate = options.verificationContext as? FIRAuthUIDelegateProtocol
    val session = (options.multiFactorSession as? MultiFactorSessionImpl)?.native as? FIRMultiFactorSession
    val hint = options.multiFactorHint?.native as? FIRPhoneMultiFactorInfo
    val completion: (String?, NSError?) -> Unit = { verificationId, error ->
        if (error != null) options.callbacks.onVerificationFailed(error.toAuthException(options.firebaseAuth)) else options.codeSent(verificationId.orEmpty())
    }
    when {
        hint != null -> provider.verifyPhoneNumberWithMultiFactorInfo(hint, delegate, session, completion)
        session != null -> provider.verifyPhoneNumber(options.phoneNumber.orEmpty(), delegate, session, completion)
        else -> provider.verifyPhoneNumber(options.phoneNumber.orEmpty(), delegate, completion)
    }
}

internal actual fun nativePhoneMultiFactorAssertion(credential: PhoneAuthCredential): Any = FIRPhoneMultiFactorGenerator.assertionWithCredential(credential.ios as FIRPhoneAuthCredential)

internal actual fun nativeTotpGenerateSecret(session: MultiFactorSession): Task<TotpSecret> = task(null) { completion ->
    FIRTOTPMultiFactorGenerator.generateSecretWithMultiFactorSession((session as MultiFactorSessionImpl).native as FIRMultiFactorSession) { secret, error -> completion(secret?.let { TotpSecretImpl(it) }, error) }
}

internal actual fun nativeTotpAssertionForEnrollment(secret: TotpSecret, oneTimePassword: String): Any = FIRTOTPMultiFactorGenerator.assertionForEnrollmentWithSecret((secret as TotpSecretImpl).ios, oneTimePassword)

internal actual fun nativeTotpAssertionForSignIn(enrollmentId: String, oneTimePassword: String): Any = FIRTOTPMultiFactorGenerator.assertionForSignInWithEnrollmentID(enrollmentId, oneTimePassword)

/** Sets the delegate that presents the reCAPTCHA of the verification when the SDK needs one; the default presents it over the key window. */
public fun PhoneAuthOptions.Builder.setUIDelegate(delegate: FIRAuthUIDelegateProtocol?): PhoneAuthOptions.Builder = apply { verificationContext = delegate }
