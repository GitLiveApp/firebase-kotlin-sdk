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
    actual fun credentialWithEmail(
        email: String,
        password: String
    ): AuthCredential =
        AuthCredential(FIREmailAuthProvider.credentialWithEmail(email = email, password = password))
}

actual object FacebookAuthProvider {
    actual fun credentialWithAccessToken(accessToken: String): AuthCredential = AuthCredential(FIRFacebookAuthProvider.credentialWithAccessToken(accessToken))
}

actual object GithubAuthProvider {
    actual fun credentialWithToken(token: String): AuthCredential = AuthCredential(FIRGitHubAuthProvider.credentialWithToken(token))
}

actual object GoogleAuthProvider {
    actual fun credentialWithIDAndAccessToken(idToken: String, accessToken: String): AuthCredential = AuthCredential(FIRGoogleAuthProvider.credentialWithIDToken(idToken, accessToken))
}

actual class OAuthProvider(val ios: FIROAuthProvider, private val auth: FirebaseAuth) {
    actual constructor(provider: String, auth: FirebaseAuth) : this(FIROAuthProvider.providerWithProviderID(provider, auth.ios), auth)

    actual companion object {
        actual fun credentialsWithAccessToken(providerId: String, accessToken: String): AuthCredential = AuthCredential(FIROAuthProvider.credentialWithProviderID(providerId, accessToken))
        actual fun credentialsWithIDAndAccessToken(providerId: String, idToken: String, accessToken: String): AuthCredential = AuthCredential(FIROAuthProvider.credentialWithProviderID(providerID = providerId, IDToken = idToken, accessToken = accessToken))
        actual fun credentialsWithIDRawNonceAndAccessToken(providerId: String, idToken: String, rawNonce: String, accessToken: String): AuthCredential = AuthCredential(FIROAuthProvider.credentialWithProviderID(providerID = providerId, IDToken = idToken, rawNonce = rawNonce, accessToken = accessToken))
        actual fun credentialsWithIDAndRawNonce(providerId: String, idToken: String, rawNonce: String): AuthCredential =  AuthCredential(FIROAuthProvider.credentialWithProviderID(providerID = providerId, IDToken = idToken, rawNonce = rawNonce))
    }

    actual fun addScope(vararg scope: String) {
        val scopes = ios.scopes?.mapNotNull { it as? String } ?: emptyList()
        ios.setScopes(scopes + scope.asList())
    }
    actual fun setCustomParameters(parameters: Map<String, String>) {
        ios.setCustomParameters(emptyMap<Any?, Any?>() + parameters)
    }

    private fun getCustomParameters(): Map<String, String> {
        val customParameters = ios.customParameters ?: emptyMap<Any?, Any?>()
        return customParameters.mapNotNull {
            val key = it.key
            val value = it.value
            if (key is String && value is String)
                key to value
            else
                null}.toMap()
    }

    actual suspend fun signIn(signInProvider: SignInProvider): AuthResult = AuthResult(ios.awaitExpectedResult { auth.ios.signInWithProvider(ios, signInProvider.delegate, it) })
}

actual class SignInProvider(val delegate: FIRAuthUIDelegateProtocol)

actual class PhoneAuthProvider(val ios: FIRPhoneAuthProvider) {

    actual constructor(auth: FirebaseAuth) : this(FIRPhoneAuthProvider.providerWithAuth(auth.ios))

    actual fun credentialWithVerificationIdAndSmsCode(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(ios.credentialWithVerificationID(verificationId, smsCode))
    actual suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential {
        val verificationId: String = ios.awaitExpectedResult { ios.verifyPhoneNumber(phoneNumber, verificationProvider.delegate, it) }
        val verificationCode = verificationProvider.getVerificationCode()
        return credentialWithVerificationIdAndSmsCode(verificationId, verificationCode)
    }
}

actual interface PhoneVerificationProvider {
    val delegate: FIRAuthUIDelegateProtocol
    suspend fun getVerificationCode(): String
}

actual object TwitterAuthProvider {
    actual fun credentialWithTokenAndSecret(token: String, secret: String): AuthCredential = AuthCredential(FIRTwitterAuthProvider.credentialWithToken(token, secret))
}