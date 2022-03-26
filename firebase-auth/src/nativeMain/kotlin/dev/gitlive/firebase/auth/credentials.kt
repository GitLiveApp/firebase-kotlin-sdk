/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.*

actual open class AuthCredential(open val ios: FIRAuthCredential) {
    actual val providerId: String
        get() = ios.provider
}

actual class PhoneAuthCredential(override val ios: FIRPhoneAuthCredential) : AuthCredential(ios)
actual class OAuthCredential(override val ios: FIROAuthCredential) : AuthCredential(ios)

actual object EmailAuthProvider {
    actual fun credential(
        email: String,
        password: String
    ): AuthCredential =
        AuthCredential(FIREmailAuthProvider.credentialWithEmail(email = email, password = password))

    actual fun credentialWithLink(
        email: String,
        emailLink: String
    ): AuthCredential =
        AuthCredential(FIREmailAuthProvider.credentialWithEmail(email = email, link = emailLink))
}

actual object FacebookAuthProvider {
    actual fun credential(accessToken: String): AuthCredential = AuthCredential(FIRFacebookAuthProvider.credentialWithAccessToken(accessToken))
}

actual object GithubAuthProvider {
    actual fun credential(token: String): AuthCredential = AuthCredential(FIRGitHubAuthProvider.credentialWithToken(token))
}

actual object GoogleAuthProvider {
    actual fun credential(idToken: String?, accessToken: String?): AuthCredential {
        requireNotNull(idToken) { "idToken must not be null" }
        requireNotNull(accessToken) { "accessToken must not be null" }
        return AuthCredential(FIRGoogleAuthProvider.credentialWithIDToken(idToken, accessToken))
    }
}

actual class OAuthProvider(val ios: FIROAuthProvider) {

    actual constructor(
        provider: String,
        scopes: List<String>,
        customParameters: Map<String, String>,
        auth: FirebaseAuth
    ) : this(FIROAuthProvider.providerWithProviderID(provider, auth.ios)) {
        ios.setScopes(scopes)
        @Suppress("UNCHECKED_CAST")
        ios.setCustomParameters(customParameters as Map<Any?, *>)
    }

    actual companion object {
        actual fun credential(providerId: String, accessToken: String?, idToken: String?, rawNonce: String?): OAuthCredential {
            val credential = when {
                idToken == null -> FIROAuthProvider.credentialWithProviderID(providerID = providerId, accessToken = accessToken!!)
                accessToken == null -> FIROAuthProvider.credentialWithProviderID(providerID = providerId, IDToken = idToken, rawNonce = rawNonce!!)
                rawNonce == null -> FIROAuthProvider.credentialWithProviderID(providerID = providerId, IDToken = idToken, accessToken = accessToken)
                else -> FIROAuthProvider.credentialWithProviderID(providerID = providerId, IDToken = idToken, rawNonce = rawNonce, accessToken = accessToken)
            }
            return OAuthCredential(credential)
        }
    }
}

actual class PhoneAuthProvider(val ios: FIRPhoneAuthProvider) {

    actual constructor(auth: FirebaseAuth) : this(FIRPhoneAuthProvider.providerWithAuth(auth.ios))

    actual fun credential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(ios.credentialWithVerificationID(verificationId, smsCode))

    actual suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential {
        val verificationId: String = ios.awaitResult { ios.verifyPhoneNumber(phoneNumber, verificationProvider.delegate, it) }
        val verificationCode = verificationProvider.getVerificationCode()
        return credential(verificationId, verificationCode)
    }
}

actual interface PhoneVerificationProvider {
    val delegate: FIRAuthUIDelegateProtocol
    suspend fun getVerificationCode(): String
}

actual object TwitterAuthProvider {
    actual fun credential(token: String, secret: String): AuthCredential = AuthCredential(FIRTwitterAuthProvider.credentialWithToken(token, secret))
}
