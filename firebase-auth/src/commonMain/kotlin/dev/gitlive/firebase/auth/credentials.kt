/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase

expect open class AuthCredential {
    val providerId: String
}
expect class PhoneAuthCredential : AuthCredential

expect class OAuthCredential : AuthCredential

expect object EmailAuthProvider {
    fun credentialWithEmail(email: String, password: String): AuthCredential
}

expect object FacebookAuthProvider {
    fun credentialWithAccessToken(accessToken: String): AuthCredential
}

expect object GithubAuthProvider {
    fun credentialWithToken(token: String): AuthCredential
}

expect object GoogleAuthProvider {
    fun credentialWithIDAndAccessToken(idToken: String, accessToken: String): AuthCredential
}

expect class OAuthProvider constructor(provider: String, auth: FirebaseAuth = Firebase.auth) {
    companion object {
        fun credentialsWithAccessToken(providerId: String, accessToken: String): AuthCredential
        fun credentialsWithIDAndAccessToken(providerId: String, idToken: String, accessToken: String): AuthCredential
        fun credentialsWithIDRawNonceAndAccessToken(providerId: String, idToken: String, rawNonce: String, accessToken: String): AuthCredential
        fun credentialsWithIDAndRawNonce(providerId: String, idToken: String, rawNonce: String): AuthCredential
    }

    fun addScope(vararg scope: String)
    fun setCustomParameters(parameters: Map<String, String>)

    suspend fun signIn(signInProvider: SignInProvider): AuthResult
}

expect class SignInProvider

expect class PhoneAuthProvider constructor(auth: FirebaseAuth = Firebase.auth) {
    fun credentialWithVerificationIdAndSmsCode(verificationId: String, smsCode: String): PhoneAuthCredential
    suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential

}

expect interface PhoneVerificationProvider

expect object TwitterAuthProvider {
    fun credentialWithTokenAndSecret(token: String, secret: String): AuthCredential
}
