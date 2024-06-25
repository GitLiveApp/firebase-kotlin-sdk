/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase

public expect open class AuthCredential {
    public val providerId: String
}
public expect class PhoneAuthCredential : AuthCredential

public expect class OAuthCredential : AuthCredential

public expect object EmailAuthProvider {
    public fun credential(email: String, password: String): AuthCredential
    public fun credentialWithLink(email: String, emailLink: String): AuthCredential
}

public expect object FacebookAuthProvider {
    public fun credential(accessToken: String): AuthCredential
}

public expect object GithubAuthProvider {
    public fun credential(token: String): AuthCredential
}

public expect object GoogleAuthProvider {
    public fun credential(idToken: String?, accessToken: String?): AuthCredential
}

public expect class OAuthProvider(
    provider: String,
    scopes: List<String> = emptyList(),
    customParameters: Map<String, String> = emptyMap(),
    auth: FirebaseAuth = Firebase.auth,
) {
    public companion object {
        public fun credential(providerId: String, accessToken: String? = null, idToken: String? = null, rawNonce: String? = null): OAuthCredential
    }
}

public expect class PhoneAuthProvider(auth: FirebaseAuth = Firebase.auth) {
    public fun credential(verificationId: String, smsCode: String): PhoneAuthCredential
    public suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential
}

public expect interface PhoneVerificationProvider

public expect object TwitterAuthProvider {
    public fun credential(token: String, secret: String): AuthCredential
}
