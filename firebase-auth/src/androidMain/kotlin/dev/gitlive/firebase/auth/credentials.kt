/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import android.app.Activity
import kotlinx.coroutines.tasks.await

actual open class AuthCredential(open val android: com.google.firebase.auth.AuthCredential) {
    actual val providerId: String
        get() = android.provider
}

actual class PhoneAuthCredential(override val android: com.google.firebase.auth.PhoneAuthCredential) : AuthCredential(android)

actual class OAuthCredential(override val android: com.google.firebase.auth.OAuthCredential) : AuthCredential(android)

actual object EmailAuthProvider {
    actual fun credentialWithEmail(
        email: String,
        password: String
    ): AuthCredential = AuthCredential(com.google.firebase.auth.EmailAuthProvider.getCredential(email, password))
}

actual object FacebookAuthProvider {
    actual fun credentialWithAccessToken(accessToken: String): AuthCredential = AuthCredential(com.google.firebase.auth.FacebookAuthProvider.getCredential(accessToken))
}

actual object GithubAuthProvider {
    actual fun credentialWithToken(token: String): AuthCredential = AuthCredential(com.google.firebase.auth.GithubAuthProvider.getCredential(token))
}

actual object GoogleAuthProvider {
    actual fun credentialWithIDAndAccessToken(idToken: String, accessToken: String): AuthCredential = AuthCredential(com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, accessToken))
}

actual class OAuthProvider(val android: com.google.firebase.auth.OAuthProvider.Builder, private val auth: FirebaseAuth) {
    actual constructor(provider: String, auth: FirebaseAuth) : this(com.google.firebase.auth.OAuthProvider.newBuilder(provider, auth.android), auth)

    actual companion object {
        actual fun credentialsWithAccessToken(providerId: String, accessToken: String): AuthCredential = createCredentials(providerId) {
            this.accessToken = accessToken
        }
        actual fun credentialsWithIDAndAccessToken(providerId: String, idToken: String, accessToken: String): AuthCredential = createCredentials(providerId) {
            setIdToken(idToken)
            this.accessToken = accessToken
        }
        actual fun credentialsWithIDRawNonceAndAccessToken(providerId: String, idToken: String, rawNonce: String, accessToken: String): AuthCredential = createCredentials(providerId) {
            setIdTokenWithRawNonce(idToken, rawNonce)
            this.accessToken = accessToken
        }
        actual fun credentialsWithIDAndRawNonce(providerId: String, idToken: String, rawNonce: String): AuthCredential = createCredentials(providerId) {
            setIdTokenWithRawNonce(idToken, rawNonce)
        }

        private fun createCredentials(providerId: String, block: com.google.firebase.auth.OAuthProvider.CredentialBuilder.() -> Unit): AuthCredential {
            val credential = com.google.firebase.auth.OAuthProvider.newCredentialBuilder(providerId).apply {
                block()
            }.build()
            return (credential as? com.google.firebase.auth.OAuthCredential)?.let { OAuthCredential(it) } ?: AuthCredential(credential)
        }
    }

    actual fun addScope(vararg scope: String) {
        android.scopes = android.scopes + scope.asList()
    }
    actual fun addCustomParameter(key: String, value: String) {
        android.addCustomParameter(key, value)
    }
    actual fun addCustomParameters(parameters: Map<String, String>) {
        android.addCustomParameters(parameters)
    }

    actual suspend fun signIn(signInProvider: SignInProvider): AuthResult = AuthResult(auth.android.startActivityForSignInWithProvider(signInProvider, android.build()).await())
}

actual typealias SignInProvider = Activity

actual class PhoneAuthProvider(val android: com.google.firebase.auth.PhoneAuthProvider) {

    actual companion object {
        actual fun credentialWithVerificationIdAndSmsCode(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId, smsCode))
    }

    actual constructor(auth: FirebaseAuth) : this(com.google.firebase.auth.PhoneAuthProvider.getInstance(auth.android))

    fun test() {
    }
}

expect object TwitterAuthProvider {
    fun credentialWithTokenAndSecret(token: String, secret: String): AuthCredential
}
