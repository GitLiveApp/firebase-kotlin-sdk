/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import com.google.firebase.auth.js as compatJs
import com.google.firebase.auth.phoneCredential
import com.google.firebase.auth.setApplicationVerifier
import dev.gitlive.firebase.auth.externals.ApplicationVerifier
import dev.gitlive.firebase.auth.externals.Auth
import dev.gitlive.firebase.auth.externals.User
import kotlinx.coroutines.CompletableDeferred
import com.google.firebase.auth.FirebaseAuth as CompatFirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions as CompatPhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider as CompatPhoneAuthProvider
import dev.gitlive.firebase.auth.externals.AuthCredential as JsAuthCredential

/** The underlying Firebase JS SDK object. */
public val FirebaseAuth.js: Auth get() = compat.js

/** The [FirebaseAuth] of the JS SDK's [js]. */
public fun FirebaseAuth(js: Auth): FirebaseAuth = FirebaseAuth(CompatFirebaseAuth.wrap(js))

public val FirebaseUser.js: User get() = compat.js

public val AuthCredential.js: JsAuthCredential get() = compat.compatJs

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
