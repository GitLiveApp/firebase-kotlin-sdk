/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

/** @property native The platform SDK's credential. */
public actual abstract class AuthCredential internal constructor(internal val native: Any) {
    public actual abstract val provider: String
    public actual abstract val signInMethod: String
}

public actual class EmailAuthCredential internal constructor(native: Any, override val signInMethod: String) : AuthCredential(native) {
    override val provider: String get() = EmailAuthProvider.PROVIDER_ID
}

public actual class FacebookAuthCredential internal constructor(native: Any) : AuthCredential(native) {
    override val provider: String get() = FacebookAuthProvider.PROVIDER_ID
    override val signInMethod: String get() = FacebookAuthProvider.FACEBOOK_SIGN_IN_METHOD
}

public actual class GithubAuthCredential internal constructor(native: Any) : AuthCredential(native) {
    override val provider: String get() = GithubAuthProvider.PROVIDER_ID
    override val signInMethod: String get() = GithubAuthProvider.GITHUB_SIGN_IN_METHOD
}

public actual class GoogleAuthCredential internal constructor(native: Any) : AuthCredential(native) {
    override val provider: String get() = GoogleAuthProvider.PROVIDER_ID
    override val signInMethod: String get() = GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD
}

public actual class TwitterAuthCredential internal constructor(native: Any) : AuthCredential(native) {
    override val provider: String get() = TwitterAuthProvider.PROVIDER_ID
    override val signInMethod: String get() = TwitterAuthProvider.TWITTER_SIGN_IN_METHOD
}

public actual class PhoneAuthCredential internal constructor(native: Any, public actual val smsCode: String?) : AuthCredential(native) {
    override val provider: String get() = PhoneAuthProvider.PROVIDER_ID
    override val signInMethod: String get() = PhoneAuthProvider.PHONE_SIGN_IN_METHOD
}

public actual abstract class OAuthCredential actual constructor() : AuthCredential(Unit) {
    public actual abstract val accessToken: String?
    public actual abstract val idToken: String?
    public actual abstract val secret: String?
}

/** A generic credential of the platform SDK, wrapped when the SDK returns one (e.g. from a sign-in result). */
internal class NativeCredential(native: Any, override val provider: String, override val signInMethod: String) : AuthCredential(native)

internal class OAuthCredentialImpl(
    private val nativeCredential: Any,
    override val provider: String,
    override val idToken: String?,
    override val accessToken: String?,
    override val secret: String?,
) : OAuthCredential() {
    internal val platformCredential: Any get() = nativeCredential
    override val signInMethod: String get() = provider
}

/** The platform SDK's credential: [OAuthCredential] is constructible by consumers, so it keeps its own. */
internal val AuthCredential.platform: Any get() = (this as? OAuthCredentialImpl)?.platformCredential ?: native

public actual class EmailAuthProvider private constructor() {
    public actual companion object {
        public actual val PROVIDER_ID: String = "password"
        public actual val EMAIL_PASSWORD_SIGN_IN_METHOD: String = "password"
        public actual val EMAIL_LINK_SIGN_IN_METHOD: String = "emailLink"

        public actual fun getCredential(email: String, password: String): AuthCredential = EmailAuthCredential(nativeEmailCredential(email, password), EMAIL_PASSWORD_SIGN_IN_METHOD)

        public actual fun getCredentialWithLink(email: String, emailLink: String): AuthCredential = EmailAuthCredential(nativeEmailLinkCredential(email, emailLink), EMAIL_LINK_SIGN_IN_METHOD)
    }
}

public actual class FacebookAuthProvider private constructor() {
    public actual companion object {
        public actual val PROVIDER_ID: String = "facebook.com"
        public actual val FACEBOOK_SIGN_IN_METHOD: String = "facebook.com"

        public actual fun getCredential(accessToken: String): AuthCredential = FacebookAuthCredential(nativeFacebookCredential(accessToken))
    }
}

public actual class GithubAuthProvider private constructor() {
    public actual companion object {
        public actual val PROVIDER_ID: String = "github.com"
        public actual val GITHUB_SIGN_IN_METHOD: String = "github.com"

        public actual fun getCredential(token: String): AuthCredential = GithubAuthCredential(nativeGithubCredential(token))
    }
}

public actual class GoogleAuthProvider private constructor() {
    public actual companion object {
        public actual val PROVIDER_ID: String = "google.com"
        public actual val GOOGLE_SIGN_IN_METHOD: String = "google.com"

        public actual fun getCredential(idToken: String?, accessToken: String?): AuthCredential = GoogleAuthCredential(nativeGoogleCredential(idToken, accessToken))
    }
}

public actual class TwitterAuthProvider private constructor() {
    public actual companion object {
        public actual val PROVIDER_ID: String = "twitter.com"
        public actual val TWITTER_SIGN_IN_METHOD: String = "twitter.com"

        public actual fun getCredential(token: String, secret: String): AuthCredential = TwitterAuthCredential(nativeTwitterCredential(token, secret))
    }
}

public actual abstract class FederatedAuthProvider actual constructor()

/**
 * A pure Kotlin value; the platform SDK's provider is built from it when it is used.
 *
 * @property scopes The OAuth scopes to request.
 * @property customParameters The provider-specific parameters of the sign-in request.
 * @property firebaseAuth The instance the provider signs in to.
 */
public actual class OAuthProvider internal constructor(
    public actual val providerId: String,
    public val scopes: List<String>,
    public val customParameters: Map<String, String>,
    public val firebaseAuth: FirebaseAuth,
) : FederatedAuthProvider() {

    public actual class Builder internal constructor(private val providerId: String, private val firebaseAuth: FirebaseAuth) {
        private var scopes: List<String> = emptyList()
        private val customParameters = mutableMapOf<String, String>()

        public actual fun setScopes(scopes: List<String>): Builder = apply { this.scopes = scopes.toList() }
        public actual fun addCustomParameter(key: String, value: String): Builder = apply { customParameters[key] = value }
        public actual fun addCustomParameters(customParameters: Map<String, String>): Builder = apply { this.customParameters += customParameters }
        public actual fun build(): OAuthProvider = OAuthProvider(providerId, scopes, customParameters.toMap(), firebaseAuth)
    }

    public actual class CredentialBuilder internal constructor(private val providerId: String) {
        private var accessToken: String? = null
        private var idToken: String? = null
        private var rawNonce: String? = null

        public actual fun setAccessToken(accessToken: String): CredentialBuilder = apply { this.accessToken = accessToken }
        public actual fun setIdToken(idToken: String): CredentialBuilder = apply { this.idToken = idToken }
        public actual fun setIdTokenWithRawNonce(idToken: String, rawNonce: String): CredentialBuilder = apply {
            this.idToken = idToken
            this.rawNonce = rawNonce
        }
        public actual fun build(): AuthCredential = OAuthCredentialImpl(nativeOAuthCredential(providerId, idToken, accessToken, rawNonce), providerId, idToken, accessToken, null)
    }

    public actual companion object {
        public actual fun newBuilder(providerId: String): Builder = Builder(providerId, FirebaseAuth.getInstance())

        public actual fun newBuilder(providerId: String, firebaseAuth: FirebaseAuth): Builder = Builder(providerId, firebaseAuth)

        public actual fun newCredentialBuilder(providerId: String): CredentialBuilder = CredentialBuilder(providerId)

        @Deprecated("Use newCredentialBuilder(providerId)", ReplaceWith("OAuthProvider.newCredentialBuilder(providerId).setIdToken(idToken).setAccessToken(accessToken).build()"))
        public actual fun getCredential(providerId: String, idToken: String, accessToken: String): AuthCredential = newCredentialBuilder(providerId).setIdToken(idToken).setAccessToken(accessToken).build()
    }
}
