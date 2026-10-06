/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

/** A credential from an identity provider, mirroring `com.google.firebase.auth.AuthCredential`. */
public expect abstract class AuthCredential {
    /** The provider id, e.g. [GoogleAuthProvider.PROVIDER_ID]. */
    public abstract val provider: String

    /** The sign-in method, e.g. [EmailAuthProvider.EMAIL_LINK_SIGN_IN_METHOD]. */
    public abstract val signInMethod: String
}

/** An email and password, or email link, credential from [EmailAuthProvider]. */
public expect class EmailAuthCredential : AuthCredential

/** A Facebook credential from [FacebookAuthProvider]. */
public expect class FacebookAuthCredential : AuthCredential

/** A GitHub credential from [GithubAuthProvider]. */
public expect class GithubAuthCredential : AuthCredential

/** A Google credential from [GoogleAuthProvider]. */
public expect class GoogleAuthCredential : AuthCredential

/** A Twitter credential from [TwitterAuthProvider]. */
public expect class TwitterAuthCredential : AuthCredential

/** A phone number credential from [PhoneAuthProvider], mirroring `com.google.firebase.auth.PhoneAuthCredential`. */
public expect class PhoneAuthCredential : AuthCredential {
    /** The SMS code the credential was created with, or null when the SDK verified the number itself. */
    public val smsCode: String?
}

/** A credential of a generic OAuth provider, mirroring `com.google.firebase.auth.OAuthCredential`. */
public expect abstract class OAuthCredential() : AuthCredential {
    public abstract val accessToken: String?
    public abstract val idToken: String?

    /** The OAuth 1 secret, for Twitter. */
    public abstract val secret: String?
}

/** Email credentials, mirroring `com.google.firebase.auth.EmailAuthProvider`. */
public expect class EmailAuthProvider {
    public companion object {
        public val PROVIDER_ID: String
        public val EMAIL_PASSWORD_SIGN_IN_METHOD: String
        public val EMAIL_LINK_SIGN_IN_METHOD: String

        /** A credential for [email] and [password]. */
        public fun getCredential(email: String, password: String): AuthCredential

        /** A credential for [email] and the sign-in link [emailLink] sent to it. */
        public fun getCredentialWithLink(email: String, emailLink: String): AuthCredential
    }
}

/** Facebook credentials, mirroring `com.google.firebase.auth.FacebookAuthProvider`. */
public expect class FacebookAuthProvider {
    public companion object {
        public val PROVIDER_ID: String
        public val FACEBOOK_SIGN_IN_METHOD: String

        /** A credential for a Facebook [accessToken]. */
        public fun getCredential(accessToken: String): AuthCredential
    }
}

/** GitHub credentials, mirroring `com.google.firebase.auth.GithubAuthProvider`. */
public expect class GithubAuthProvider {
    public companion object {
        public val PROVIDER_ID: String
        public val GITHUB_SIGN_IN_METHOD: String

        /** A credential for a GitHub [token]. */
        public fun getCredential(token: String): AuthCredential
    }
}

/** Google credentials, mirroring `com.google.firebase.auth.GoogleAuthProvider`. */
public expect class GoogleAuthProvider {
    public companion object {
        public val PROVIDER_ID: String
        public val GOOGLE_SIGN_IN_METHOD: String

        /** A credential for a Google [idToken] and/or [accessToken]. */
        public fun getCredential(idToken: String?, accessToken: String?): AuthCredential
    }
}

/** Twitter credentials, mirroring `com.google.firebase.auth.TwitterAuthProvider`. */
public expect class TwitterAuthProvider {
    public companion object {
        public val PROVIDER_ID: String
        public val TWITTER_SIGN_IN_METHOD: String

        /** A credential for a Twitter [token] and [secret]. */
        public fun getCredential(token: String, secret: String): AuthCredential
    }
}

/** Provider id of Firebase itself, mirroring `com.google.firebase.auth.FirebaseAuthProvider`. */
public interface FirebaseAuthProvider {
    public companion object {
        public const val PROVIDER_ID: String = "firebase"
    }
}

/** An identity provider the SDK signs in with through a web flow, mirroring `com.google.firebase.auth.FederatedAuthProvider`. */
public expect abstract class FederatedAuthProvider()

/**
 * A generic OAuth provider (Microsoft, Apple, Yahoo, or any OpenID Connect provider), mirroring
 * `com.google.firebase.auth.OAuthProvider`: built with [Builder] or the `oAuthProvider` extension, its credentials with
 * [CredentialBuilder] or the `oAuthCredential` extension.
 */
public expect class OAuthProvider : FederatedAuthProvider {
    public val providerId: String

    /** Builds an [OAuthProvider], from [newBuilder]. */
    public class Builder {
        /** The OAuth scopes to request. */
        public fun setScopes(scopes: List<String>): Builder

        /** Adds a provider-specific parameter of the sign-in request. */
        public fun addCustomParameter(key: String, value: String): Builder

        /** Adds provider-specific parameters of the sign-in request. */
        public fun addCustomParameters(customParameters: Map<String, String>): Builder

        public fun build(): OAuthProvider
    }

    /** Builds an [OAuthCredential] from the tokens of a provider, from [newCredentialBuilder]. */
    public class CredentialBuilder {
        public fun setAccessToken(accessToken: String): CredentialBuilder
        public fun setIdToken(idToken: String): CredentialBuilder

        /** Sets [idToken] with the [rawNonce] it was requested with, for Sign in with Apple. */
        public fun setIdTokenWithRawNonce(idToken: String, rawNonce: String): CredentialBuilder
        public fun build(): AuthCredential
    }

    public companion object {
        /** A builder of a provider for [providerId] and the default [FirebaseAuth]. */
        public fun newBuilder(providerId: String): Builder

        /** A builder of a provider for [providerId] and [firebaseAuth]. */
        public fun newBuilder(providerId: String, firebaseAuth: FirebaseAuth): Builder

        /** A builder of a credential for [providerId]. */
        public fun newCredentialBuilder(providerId: String): CredentialBuilder

        /** A credential for [providerId] from [idToken] and [accessToken]. */
        @Deprecated("Use newCredentialBuilder(providerId)", ReplaceWith("OAuthProvider.newCredentialBuilder(providerId).setIdToken(idToken).setAccessToken(accessToken).build()"))
        public fun getCredential(providerId: String, idToken: String, accessToken: String): AuthCredential
    }
}
