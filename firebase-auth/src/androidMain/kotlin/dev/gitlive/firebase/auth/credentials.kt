/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.OAuthProvider
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
    actual fun credential(
        email: String,
        password: String
    ): AuthCredential = AuthCredential(com.google.firebase.auth.EmailAuthProvider.getCredential(email, password))

    actual fun credentialWithLink(
        email: String,
        emailLink: String
    ): AuthCredential = AuthCredential(com.google.firebase.auth.EmailAuthProvider.getCredentialWithLink(email, emailLink))
}

actual object FacebookAuthProvider {
    actual fun credential(accessToken: String): AuthCredential = AuthCredential(com.google.firebase.auth.FacebookAuthProvider.getCredential(accessToken))
}

actual object GithubAuthProvider {
    actual fun credential(token: String): AuthCredential = AuthCredential(com.google.firebase.auth.GithubAuthProvider.getCredential(token))
}

actual object GoogleAuthProvider {
    actual fun credential(idToken: String?, accessToken: String?): AuthCredential {
        require(idToken != null || accessToken != null) {
            "Both parameters are optional but at least one must be present."
        }
        return AuthCredential(com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, accessToken))
    }
}

actual class OAuthProvider(val android: com.google.firebase.auth.OAuthProvider) {

    actual constructor(
        provider: String,
        scopes: List<String>,
        customParameters: Map<String, String>,
        auth: FirebaseAuth
    ) : this(
        com.google.firebase.auth.OAuthProvider
            .newBuilder(provider, auth.android)
            .setScopes(scopes)
            .addCustomParameters(customParameters)
            .build()
    )

    actual companion object {
        actual fun credential(providerId: String, accessToken: String?, idToken: String?, rawNonce: String?): OAuthCredential {
            val builder = OAuthProvider.newCredentialBuilder(providerId)
            accessToken?.let { builder.setAccessToken(it) }
            idToken?.let { builder.setIdToken(it) }
            rawNonce?.let { builder.setIdTokenWithRawNonce(idToken!!, it)  }
            return OAuthCredential(builder.build() as com.google.firebase.auth.OAuthCredential)
        }
    }
}

actual class PhoneAuthProvider(val android: com.google.firebase.auth.PhoneAuthProvider) {

    actual constructor(auth: FirebaseAuth) : this(com.google.firebase.auth.PhoneAuthProvider.getInstance(auth.android))

    actual fun credential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId, smsCode))

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
                        response.complete(Result.success(credential(verificationId, code)))
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
    actual fun credential(token: String, secret: String): AuthCredential = AuthCredential(com.google.firebase.auth.TwitterAuthProvider.getCredential(token, secret))
}
