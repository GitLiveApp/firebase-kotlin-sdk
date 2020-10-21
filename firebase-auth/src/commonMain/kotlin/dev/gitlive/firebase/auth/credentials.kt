/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase
import kotlin.jvm.JvmName
import kotlin.js.JsName

expect open class AuthCredential {
    val providerId: String
}
expect class PhoneAuthCredential : AuthCredential

expect class OAuthCredential : AuthCredential

expect object EmailAuthProvider {
    fun credential(email: String, password: String): AuthCredential
}

expect object FacebookAuthProvider {
    fun credential(accessToken: String): AuthCredential
}

expect object GithubAuthProvider {
    fun credential(token: String): AuthCredential
}

expect object GoogleAuthProvider {
    fun credential(idToken: String, accessToken: String): AuthCredential
}

sealed class OAuthCredentialsType {
    abstract val providerId: String
    data class AccessToken(val accessToken: String, override val providerId: String) : OAuthCredentialsType()
    data class IdAndAccessToken(val idToken: String, val accessToken: String, override val providerId: String) : OAuthCredentialsType()
    data class IdAndAccessTokenAndRawNonce(val idToken: String, val accessToken: String, val rawNonce: String, override val providerId: String) : OAuthCredentialsType()
    data class IdTokenAndRawNonce(val idToken: String, val rawNonce: String, override val providerId: String) : OAuthCredentialsType()

}

expect class OAuthProvider constructor(provider: String, auth: FirebaseAuth = Firebase.auth) {
    companion object {
        fun credentials(type: OAuthCredentialsType): AuthCredential
    }

    fun addScope(vararg scope: String)
    fun setCustomParameters(parameters: Map<String, String>)

    suspend fun signIn(signInProvider: SignInProvider): AuthResult
}

expect class SignInProvider

expect class PhoneAuthProvider constructor(auth: FirebaseAuth = Firebase.auth) {
    fun credential(verificationId: String, smsCode: String): PhoneAuthCredential
    suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential

}

expect interface PhoneVerificationProvider

expect object TwitterAuthProvider {
    fun credential(token: String, secret: String): AuthCredential
}
