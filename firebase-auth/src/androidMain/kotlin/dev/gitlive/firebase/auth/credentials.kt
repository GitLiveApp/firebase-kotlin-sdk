/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.OAuthProvider as AndroidOAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.supervisorScope
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

public val OAuthProvider.android: AndroidOAuthProvider get() = android

public actual class OAuthProvider(internal val android: AndroidOAuthProvider) {

    public actual constructor(
        provider: String,
        scopes: List<String>,
        customParameters: Map<String, String>,
        auth: FirebaseAuth,
    ) : this(
        AndroidOAuthProvider
            .newBuilder(provider, auth.android)
            .setScopes(scopes)
            .addCustomParameters(customParameters)
            .build(),
    )

    public actual companion object {
        public actual fun credential(providerId: String, accessToken: String?, idToken: String?, rawNonce: String?): OAuthCredential {
            val builder = AndroidOAuthProvider.newCredentialBuilder(providerId)
            accessToken?.let { builder.setAccessToken(it) }
            idToken?.let { builder.setIdToken(it) }
            rawNonce?.let { builder.setIdTokenWithRawNonce(idToken!!, it) }
            return OAuthCredential(builder.build() as com.google.firebase.auth.OAuthCredential)
        }
    }
}

public actual class PhoneAuthProvider(public val createOptionsBuilder: () -> PhoneAuthOptions.Builder) {

    public actual constructor(auth: FirebaseAuth) : this({ PhoneAuthOptions.newBuilder(auth.android) })

    public actual fun credential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(PhoneAuthProvider.getCredential(verificationId, smsCode))

    // unlike the other platforms android can complete the verification without any user input, via
    // sms auto retrieval, so the credential is whichever of the two arrives first
    public actual suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential = supervisorScope {
        // resending replaces the verification id and invalidates the previous one
        val latestVerificationId = MutableStateFlow<String?>(null)
        val autoRetrieved = CompletableDeferred<AuthCredential>()
        val callback = object :
            PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onCodeSent(verificationId: String, forceResending: PhoneAuthProvider.ForceResendingToken) {
                latestVerificationId.value = verificationId
                verificationProvider.codeSent {
                    val options = createOptionsBuilder()
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(verificationProvider.timeout, verificationProvider.unit)
                        .setActivity(verificationProvider.activity)
                        .setCallbacks(this)
                        .setForceResendingToken(forceResending)
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }
            }

            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                autoRetrieved.complete(AuthCredential(credential))
            }

            override fun onVerificationFailed(error: FirebaseException) {
                autoRetrieved.completeExceptionally(error)
            }
        }
        val options = createOptionsBuilder()
            .setPhoneNumber(phoneNumber)
            .setTimeout(verificationProvider.timeout, verificationProvider.unit)
            .setActivity(verificationProvider.activity)
            .setCallbacks(callback)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        val userEntered = async {
            // prompt as soon as a code has been sent rather than waiting for auto retrieval to time
            // out, as recommended by
            // https://firebase.google.com/docs/auth/android/phone-auth#oncodeautoretrievaltimeoutstring-verificationid
            latestVerificationId.filterNotNull().first()
            val code = verificationProvider.getVerificationCode()
            credential(checkNotNull(latestVerificationId.value), code)
        }

        try {
            select {
                autoRetrieved.onAwait { it }
                userEntered.onAwait { it }
            }
        } finally {
            // select does not cancel the losing clause, and a code entry still waiting on the user
            // would otherwise keep this scope alive after auto retrieval has already completed
            userEntered.cancel()
        }
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
