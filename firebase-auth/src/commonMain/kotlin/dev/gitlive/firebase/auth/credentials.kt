/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import com.google.firebase.auth.emailCredential
import com.google.firebase.auth.emailLinkCredential
import com.google.firebase.auth.facebookCredential
import com.google.firebase.auth.githubCredential
import com.google.firebase.auth.googleCredential
import com.google.firebase.auth.oAuthCredentialBuilder
import com.google.firebase.auth.oAuthProviderBuilder
import com.google.firebase.auth.twitterCredential
import dev.gitlive.firebase.Firebase
import com.google.firebase.auth.AuthCredential as CompatAuthCredential
import com.google.firebase.auth.OAuthCredential as CompatOAuthCredential
import com.google.firebase.auth.OAuthProvider as CompatOAuthProvider
import com.google.firebase.auth.PhoneAuthCredential as CompatPhoneAuthCredential

/** @property compat The Android-SDK-shaped [com.google.firebase.auth.AuthCredential] this credential wraps. */
public open class AuthCredential internal constructor(public open val compat: CompatAuthCredential) {
    public val providerId: String
        get() = compat.provider
}

public class PhoneAuthCredential internal constructor(override val compat: CompatPhoneAuthCredential) : AuthCredential(compat)

public class OAuthCredential internal constructor(override val compat: CompatOAuthCredential) : AuthCredential(compat)

public object EmailAuthProvider {
    public fun credential(email: String, password: String): AuthCredential = AuthCredential(emailCredential(email, password))

    public fun credentialWithLink(email: String, emailLink: String): AuthCredential = AuthCredential(emailLinkCredential(email, emailLink))
}

public object FacebookAuthProvider {
    public fun credential(accessToken: String): AuthCredential = AuthCredential(facebookCredential(accessToken))
}

public object GithubAuthProvider {
    public fun credential(token: String): AuthCredential = AuthCredential(githubCredential(token))
}

public object GoogleAuthProvider {
    public fun credential(idToken: String?, accessToken: String?): AuthCredential {
        require(idToken != null || accessToken != null) {
            "Both parameters are optional but at least one must be present."
        }
        return AuthCredential(googleCredential(idToken, accessToken))
    }
}

/** @property compat The Android-SDK-shaped [com.google.firebase.auth.OAuthProvider] this provider wraps. */
public class OAuthProvider internal constructor(public val compat: CompatOAuthProvider) {

    public constructor(
        provider: String,
        scopes: List<String> = emptyList(),
        customParameters: Map<String, String> = emptyMap(),
        auth: FirebaseAuth = Firebase.auth,
    ) : this(
        oAuthProviderBuilder(provider, auth.compat)
            .setScopes(scopes)
            .addCustomParameters(customParameters)
            .build(),
    )

    public companion object {
        public fun credential(providerId: String, accessToken: String? = null, idToken: String? = null, rawNonce: String? = null): OAuthCredential {
            val builder = oAuthCredentialBuilder(providerId)
            accessToken?.let { builder.setAccessToken(it) }
            idToken?.let { builder.setIdToken(it) }
            rawNonce?.let { builder.setIdTokenWithRawNonce(idToken!!, it) }
            return OAuthCredential(builder.build() as CompatOAuthCredential)
        }
    }
}

public expect class PhoneAuthProvider(auth: FirebaseAuth = Firebase.auth) {
    public fun credential(verificationId: String, smsCode: String): PhoneAuthCredential
    public suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential
}

public expect interface PhoneVerificationProvider

public object TwitterAuthProvider {
    public fun credential(token: String, secret: String): AuthCredential = AuthCredential(twitterCredential(token, secret))
}
