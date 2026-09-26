/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

// The facade name of the former androidMain file, kept for binary compatibility.
@file:JvmName("android")
@file:JvmMultifileClass

package dev.gitlive.firebase.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.phoneCredential
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.AdditionalUserInfo as AndroidAdditionalUserInfo
import com.google.firebase.auth.AuthCredential as AndroidAuthCredential
import com.google.firebase.auth.AuthResult as AndroidAuthResult
import com.google.firebase.auth.FirebaseAuth as AndroidFirebaseAuth
import com.google.firebase.auth.FirebaseUser as AndroidFirebaseUser
import com.google.firebase.auth.FirebaseUserMetadata as AndroidFirebaseUserMetadata
import com.google.firebase.auth.GetTokenResult as AndroidGetTokenResult
import com.google.firebase.auth.MultiFactor as AndroidMultiFactor
import com.google.firebase.auth.MultiFactorAssertion as AndroidMultiFactorAssertion
import com.google.firebase.auth.MultiFactorInfo as AndroidMultiFactorInfo
import com.google.firebase.auth.MultiFactorResolver as AndroidMultiFactorResolver
import com.google.firebase.auth.MultiFactorSession as AndroidMultiFactorSession
import com.google.firebase.auth.OAuthProvider as AndroidOAuthProvider
import com.google.firebase.auth.PhoneAuthCredential as AndroidPhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider as AndroidPhoneAuthProvider
import com.google.firebase.auth.UserInfo as AndroidUserInfo
import com.google.firebase.auth.verifyPhoneNumber as startPhoneVerification

// Shipped code: it binds to the real Android SDK classes at runtime, so it reaches static members through AuthStatics
// (via the internal functions of com.google.firebase.auth), never through the header stubs' Companion objects.

/** The underlying Firebase Android SDK object. */
public val FirebaseAuth.android: AndroidFirebaseAuth get() = compat

public val AuthResult.android: AndroidAuthResult get() = compat

public val AdditionalUserInfo.android: AndroidAdditionalUserInfo get() = compat

public val AuthTokenResult.android: AndroidGetTokenResult get() = compat

public val AuthCredential.android: AndroidAuthCredential get() = compat

public val OAuthProvider.android: AndroidOAuthProvider get() = compat

public val FirebaseUser.android: AndroidFirebaseUser get() = compat

public val UserInfo.android: AndroidUserInfo get() = compat

public val UserMetaData.android: AndroidFirebaseUserMetadata get() = compat

public val MultiFactor.android: AndroidMultiFactor get() = compat

public val MultiFactorInfo.android: AndroidMultiFactorInfo get() = compat

public val MultiFactorAssertion.android: AndroidMultiFactorAssertion get() = compat

public val MultiFactorSession.android: AndroidMultiFactorSession get() = compat

public val MultiFactorResolver.android: AndroidMultiFactorResolver get() = compat

public actual class PhoneAuthProvider(public val createOptionsBuilder: () -> PhoneAuthOptions.Builder) {

    public actual constructor(auth: FirebaseAuth) : this({ PhoneAuthOptions.Builder(auth.compat) })

    public actual fun credential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(phoneCredential(verificationId, smsCode))

    // unlike the other platforms android can complete the verification without any user input, via
    // sms auto retrieval, so the credential is whichever of the two arrives first
    public actual suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential = supervisorScope {
        // resending replaces the verification id and invalidates the previous one
        val latestVerificationId = MutableStateFlow<String?>(null)
        val autoRetrieved = CompletableDeferred<AuthCredential>()
        val callback = object :
            AndroidPhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onCodeSent(verificationId: String, token: AndroidPhoneAuthProvider.ForceResendingToken) {
                latestVerificationId.value = verificationId
                verificationProvider.codeSent {
                    val options = createOptionsBuilder()
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(verificationProvider.timeout, verificationProvider.unit)
                        .setActivity(verificationProvider.activity)
                        .setCallbacks(this)
                        .setForceResendingToken(token)
                        .build()
                    startPhoneVerification(options)
                }
            }

            override fun onVerificationCompleted(credential: AndroidPhoneAuthCredential) {
                autoRetrieved.complete(PhoneAuthCredential(credential))
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                autoRetrieved.completeExceptionally(exception)
            }
        }
        val options = createOptionsBuilder()
            .setPhoneNumber(phoneNumber)
            .setTimeout(verificationProvider.timeout, verificationProvider.unit)
            .setActivity(verificationProvider.activity)
            .setCallbacks(callback)
            .build()
        startPhoneVerification(options)

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
