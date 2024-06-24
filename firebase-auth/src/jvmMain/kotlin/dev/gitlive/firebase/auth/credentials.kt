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

public actual open class AuthCredential(public open val android: com.google.firebase.auth.AuthCredential) {
    public actual val providerId: String
        get() = android.provider
}

public actual class PhoneAuthCredential(override val android: com.google.firebase.auth.PhoneAuthCredential) : AuthCredential(android)

public actual class OAuthCredential(override val android: com.google.firebase.auth.OAuthCredential) : AuthCredential(android)

public actual object EmailAuthProvider {
    public actual fun credential(
        email: String,
        password: String,
    ): AuthCredential = AuthCredential(com.google.firebase.auth.EmailAuthProvider.getCredential(email, password))

    public actual fun credentialWithLink(
        email: String,
        emailLink: String,
    ): AuthCredential = AuthCredential(com.google.firebase.auth.EmailAuthProvider.getCredentialWithLink(email, emailLink))
}

public actual object FacebookAuthProvider {
    public actual fun credential(accessToken: String): AuthCredential = AuthCredential(com.google.firebase.auth.FacebookAuthProvider.getCredential(accessToken))
}

public actual object GithubAuthProvider {
    public actual fun credential(token: String): AuthCredential = AuthCredential(com.google.firebase.auth.GithubAuthProvider.getCredential(token))
}

public actual object GoogleAuthProvider {
    public actual fun credential(idToken: String?, accessToken: String?): AuthCredential {
        require(idToken != null || accessToken != null) {
            "Both parameters are optional but at least one must be present."
        }
        return AuthCredential(com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, accessToken))
    }
}

public actual class OAuthProvider(public val android: com.google.firebase.auth.OAuthProvider) {

    public actual constructor(
        provider: String,
        scopes: List<String>,
        customParameters: Map<String, String>,
        auth: FirebaseAuth,
    ) : this(
        com.google.firebase.auth.OAuthProvider
            .newBuilder(provider, auth.android)
            .setScopes(scopes)
            .addCustomParameters(customParameters)
            .build(),
    )

    public actual companion object {
        public actual fun credential(providerId: String, accessToken: String?, idToken: String?, rawNonce: String?): OAuthCredential {
            val builder = OAuthProvider.newCredentialBuilder(providerId)
            accessToken?.let { builder.setAccessToken(it) }
            idToken?.let { builder.setIdToken(it) }
            rawNonce?.let { builder.setIdTokenWithRawNonce(idToken!!, it) }
            return OAuthCredential(builder.build() as com.google.firebase.auth.OAuthCredential)
        }
    }
}

public actual class PhoneAuthProvider(public val android: com.google.firebase.auth.PhoneAuthProvider) {

    public actual constructor(auth: FirebaseAuth) : this(com.google.firebase.auth.PhoneAuthProvider.getInstance(auth.android))

    public actual fun credential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId, smsCode))

    public actual suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential = coroutineScope {
        val response = CompletableDeferred<Result<AuthCredential>>()
        val callback = object :
            PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onCodeSent(verificationId: String, forceResending: PhoneAuthProvider.ForceResendingToken) {
                verificationProvider.codeSent {
                    android.verifyPhoneNumber(phoneNumber, verificationProvider.timeout, verificationProvider.unit, verificationProvider.activity, this, forceResending)
                }
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

public actual interface PhoneVerificationProvider {
    public val activity: Activity
    public val timeout: Long
    public val unit: TimeUnit
    public fun codeSent(triggerResend: (Unit) -> Unit)
    public suspend fun getVerificationCode(): String
}

public actual object TwitterAuthProvider {
    public actual fun credential(token: String, secret: String): AuthCredential = AuthCredential(com.google.firebase.auth.TwitterAuthProvider.getCredential(token, secret))
}
