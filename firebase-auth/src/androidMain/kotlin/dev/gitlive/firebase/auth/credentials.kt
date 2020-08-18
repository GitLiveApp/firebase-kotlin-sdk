/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

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

    private var customParameters: Map<String, String> = emptyMap()

    actual fun addScope(vararg scope: String) {
        android.scopes = android.scopes + scope.asList()
    }
    actual fun setCustomParameters(parameters: Map<String, String>) {
        customParameters = parameters
    }

    actual suspend fun signIn(signInProvider: SignInProvider): AuthResult = AuthResult(auth.android.startActivityForSignInWithProvider(signInProvider, android.apply { addCustomParameters(customParameters) }.build()).await())
}

actual typealias SignInProvider = Activity

actual class PhoneAuthProvider(val android: com.google.firebase.auth.PhoneAuthProvider) {


    actual constructor(auth: FirebaseAuth) : this(com.google.firebase.auth.PhoneAuthProvider.getInstance(auth.android))

    actual fun credentialWithVerificationIdAndSmsCode(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId, smsCode))
    actual suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential = coroutineScope {
        val response = CompletableDeferred<Result<AuthCredential>>()
        val callback = object :
            PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onCodeSent(verificationId: String, forceResending: PhoneAuthProvider.ForceResendingToken) {
                verificationProvider.codeSent { android.verifyPhoneNumber(phoneNumber, verificationProvider.timeout, verificationProvider.unit, verificationProvider.activity, this, forceResending) }
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                launch {
                    val code = verificationProvider.getVerificationCode()
                    try {
                        val credentials =
                            credentialWithVerificationIdAndSmsCode(verificationId, code)
                        response.complete(Result.success(credentials))
                    } catch (e: Exception) {
                        response.complete(Result.failure(e))
                    }
                }
            }

            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                response.complete(Result.success(AuthCredential(credential)))
            }

            override fun onVerificationFailed(error: FirebaseException) {
                response.complete(Result.failure(error))
            }

        }
        android.verifyPhoneNumber(phoneNumber, verificationProvider.timeout, verificationProvider.unit, verificationProvider.activity, callback)

        response.await().getOrThrow()
    }
}

actual interface PhoneVerificationProvider {
    val activity: Activity
    val timeout: Long
    val unit: TimeUnit
    fun codeSent(triggerResend: (Unit) -> Unit)
    suspend fun getVerificationCode(): String
}

actual object TwitterAuthProvider {
    actual fun credentialWithTokenAndSecret(token: String, secret: String): AuthCredential = AuthCredential(com.google.firebase.auth.TwitterAuthProvider.getCredential(token, secret))
}
