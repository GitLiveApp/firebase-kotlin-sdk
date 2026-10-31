/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import com.google.firebase.auth.AdditionalUserInfoImpl
import com.google.firebase.auth.AuthResultImpl
import com.google.firebase.auth.FirebaseUserImpl
import com.google.firebase.auth.FirebaseUserMetadataImpl
import com.google.firebase.auth.MultiFactorImpl
import com.google.firebase.auth.MultiFactorResolverImpl
import com.google.firebase.auth.MultiFactorSessionImpl
import com.google.firebase.auth.NativeGetTokenResult
import com.google.firebase.auth.UserInfoImpl
import com.google.firebase.auth.phoneCredential
import com.google.firebase.auth.setApplicationVerifier
import com.google.firebase.auth.toCompat
import dev.gitlive.firebase.auth.externals.ApplicationVerifier
import dev.gitlive.firebase.auth.externals.Auth
import dev.gitlive.firebase.auth.externals.IdTokenResult
import dev.gitlive.firebase.auth.externals.MultiFactorUser
import dev.gitlive.firebase.auth.externals.User
import dev.gitlive.firebase.auth.externals.UserMetadata
import kotlinx.coroutines.CompletableDeferred
import com.google.firebase.auth.FirebaseAuth as CompatFirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions as CompatPhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider as CompatPhoneAuthProvider
import com.google.firebase.auth.js as compatJs
import dev.gitlive.firebase.auth.externals.AdditionalUserInfo as JsAdditionalUserInfo
import dev.gitlive.firebase.auth.externals.AuthCredential as JsAuthCredential
import dev.gitlive.firebase.auth.externals.AuthResult as JsAuthResult
import dev.gitlive.firebase.auth.externals.MultiFactorAssertion as JsMultiFactorAssertion
import dev.gitlive.firebase.auth.externals.MultiFactorInfo as JsMultiFactorInfo
import dev.gitlive.firebase.auth.externals.MultiFactorResolver as JsMultiFactorResolver
import dev.gitlive.firebase.auth.externals.MultiFactorSession as JsMultiFactorSession
import dev.gitlive.firebase.auth.externals.OAuthProvider as JsOAuthProvider
import dev.gitlive.firebase.auth.externals.UserInfo as JsUserInfo

/** The Firebase JS SDK object this wraps. */
public val FirebaseAuth.js: Auth get() = compat.js

/** The [FirebaseAuth] of the JS SDK's [js]. */
public fun FirebaseAuth(js: Auth): FirebaseAuth = FirebaseAuth(CompatFirebaseAuth.wrap(js))

public val FirebaseUser.js: User get() = compat.js

public val UserInfo.js: JsUserInfo
    get() = when (val info = compat) {
        is FirebaseUserImpl -> info.js
        else -> (info as UserInfoImpl).native as JsUserInfo
    }

public val UserMetaData.js: UserMetadata get() = (compat as FirebaseUserMetadataImpl).native as UserMetadata

/** The JS SDK's result; the result of `unlink`, which the SDK reports as a user, has none. */
public val AuthResult.js: JsAuthResult get() = checkNotNull((compat as AuthResultImpl).native) { "unlink has no UserCredential" } as JsAuthResult

public val AdditionalUserInfo.js: JsAdditionalUserInfo get() = (compat as AdditionalUserInfoImpl).native as JsAdditionalUserInfo

public val AuthTokenResult.js: IdTokenResult get() = (compat as NativeGetTokenResult).native as IdTokenResult

public val AuthCredential.js: JsAuthCredential get() = compat.compatJs

/** The [AuthCredential] of the JS SDK's [js], e.g. one obtained from a provider SDK. */
public fun AuthCredential(js: JsAuthCredential): AuthCredential = AuthCredential(js.toCompat())

public val MultiFactor.js: MultiFactorUser get() = (compat as MultiFactorImpl).js

public val MultiFactorInfo.js: JsMultiFactorInfo get() = compat.native as JsMultiFactorInfo

public val MultiFactorAssertion.js: JsMultiFactorAssertion get() = compat.native as JsMultiFactorAssertion

public val MultiFactorSession.js: JsMultiFactorSession get() = (compat as MultiFactorSessionImpl).native as JsMultiFactorSession

public val MultiFactorResolver.js: JsMultiFactorResolver get() = (compat as MultiFactorResolverImpl).js

/** The JS SDK's provider configured as this one. */
public val OAuthProvider.js: JsOAuthProvider
    get() = JsOAuthProvider(compat.providerId).apply {
        compat.scopes.forEach { addScope(it) }
        setCustomParameters(compat.customParameters.toJsParameters())
    }

private fun Map<String, String>.toJsParameters(): Map<String, String> = this

public actual class PhoneAuthProvider(private val createOptionsBuilder: () -> CompatPhoneAuthOptions.Builder) {

    public actual constructor(auth: FirebaseAuth) : this({ CompatPhoneAuthOptions.Builder(auth.compat) })

    public actual fun credential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(phoneCredential(verificationId, smsCode))

    public actual suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential {
        val sentVerificationId = CompletableDeferred<String>()
        val options = createOptionsBuilder()
            .setPhoneNumber(phoneNumber)
            .setApplicationVerifier(verificationProvider.verifier)
            .setCallbacks(
                object : CompatPhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onCodeSent(verificationId: String, token: CompatPhoneAuthProvider.ForceResendingToken) {
                        sentVerificationId.complete(verificationId)
                    }

                    override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {}

                    override fun onVerificationFailed(exception: com.google.firebase.FirebaseException) {
                        sentVerificationId.completeExceptionally(exception)
                    }
                },
            )
            .build()
        CompatPhoneAuthProvider.verifyPhoneNumber(options)
        val id = sentVerificationId.await()
        return credential(id, verificationProvider.getVerificationCode(id))
    }
}

public actual interface PhoneVerificationProvider {
    public val verifier: ApplicationVerifier
    public suspend fun getVerificationCode(verificationId: String): String
}
