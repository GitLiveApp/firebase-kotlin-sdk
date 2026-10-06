/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import com.google.android.gms.tasks.Task

/** The profile of a user from one provider, mirroring `com.google.firebase.auth.UserInfo`. The photo URL is Android-only. */
public expect interface UserInfo {
    public val uid: String
    public val providerId: String
    public val displayName: String?
    public val email: String?
    public val phoneNumber: String?
    public val isEmailVerified: Boolean
}

/**
 * A signed-in user, mirroring `com.google.firebase.auth.FirebaseUser` from the Firebase Android SDK: its profile and
 * provider data, and the account operations that need a signed-in user.
 *
 * Not mirrored: the `Activity`-bound provider flows and the photo URL, an Android `Uri` (see `api/android-sdk/exclusions.txt`).
 */
public expect abstract class FirebaseUser() : UserInfo {
    /** The tenant the user signed in to, or null. */
    public abstract val tenantId: String?

    /** Whether the user signed in anonymously. */
    public abstract val isAnonymous: Boolean

    /** When the account was created and last signed in, or null when unknown. */
    public abstract val metadata: FirebaseUserMetadata?

    /** The multi-factor enrollment of the user. */
    public abstract val multiFactor: MultiFactor

    /** The profile of the user from each linked provider. */
    public abstract val providerData: List<UserInfo>

    /** Deletes the account; requires a recent sign-in. */
    public fun delete(): Task<Nothing?>

    /** The ID token of the user, refreshed when [forceRefresh]. */
    public fun getIdToken(forceRefresh: Boolean): Task<GetTokenResult>

    /** Links [credential] to the account. */
    public fun linkWithCredential(credential: AuthCredential): Task<AuthResult>

    /** Re-authenticates with [credential], for the operations that require a recent sign-in. */
    public fun reauthenticate(credential: AuthCredential): Task<Nothing?>

    /** Re-authenticates with [credential] and completes with the result. */
    public fun reauthenticateAndRetrieveData(credential: AuthCredential): Task<AuthResult>

    /** Refreshes the profile from the backend. */
    public fun reload(): Task<Nothing?>

    /** Sends a verification email to the user's address. */
    public fun sendEmailVerification(): Task<Nothing?>

    /** Sends a verification email whose link follows [actionCodeSettings], when given. */
    public fun sendEmailVerification(actionCodeSettings: ActionCodeSettings?): Task<Nothing?>

    /** Unlinks the provider [provider] and completes with the updated user. */
    public fun unlink(provider: String): Task<AuthResult>

    /** Sets the email address; requires a recent sign-in. */
    @Deprecated("Use verifyBeforeUpdateEmail instead", ReplaceWith("verifyBeforeUpdateEmail(email)"))
    public fun updateEmail(email: String): Task<Nothing?>

    /** Sets the password; requires a recent sign-in. */
    public fun updatePassword(password: String): Task<Nothing?>

    /** Sets the phone number from a verified [credential]. */
    public fun updatePhoneNumber(credential: PhoneAuthCredential): Task<Nothing?>

    /** Applies [request] to the profile. */
    public fun updateProfile(request: UserProfileChangeRequest): Task<Nothing?>

    /** Sends a verification email to [newEmail], which becomes the address once verified. */
    public fun verifyBeforeUpdateEmail(newEmail: String): Task<Nothing?>

    /** Sends a verification email to [newEmail] whose link follows [actionCodeSettings], when given. */
    public fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings?): Task<Nothing?>
}

/** When an account was created and last signed in, mirroring `com.google.firebase.auth.FirebaseUserMetadata`. */
public interface FirebaseUserMetadata {
    /** Milliseconds since the epoch, or 0 when unknown. */
    public val creationTimestamp: Long

    /** Milliseconds since the epoch, or 0 when unknown. */
    public val lastSignInTimestamp: Long
}

/** The result of a sign-in, link or re-authentication, mirroring `com.google.firebase.auth.AuthResult`. */
public interface AuthResult {
    public val user: FirebaseUser?

    /** The credential the provider returned, when it returns one. */
    public val credential: AuthCredential?

    public val additionalUserInfo: AdditionalUserInfo?
}

/** What a provider reported about the user on sign-in, mirroring `com.google.firebase.auth.AdditionalUserInfo`. */
public interface AdditionalUserInfo {
    public val providerId: String?
    public val username: String?
    public val profile: Map<String, Any?>?
    public val isNewUser: Boolean
}

/** The result of [FirebaseAuth.fetchSignInMethodsForEmail], mirroring `com.google.firebase.auth.SignInMethodQueryResult`. */
public interface SignInMethodQueryResult {
    public val signInMethods: List<String>?
}

/**
 * An ID token and its claims, mirroring `com.google.firebase.auth.GetTokenResult`.
 *
 * @property token The JWT, or null when unavailable.
 * @property claims The claims of the token, including the custom claims.
 */
public class GetTokenResult(public val token: String?, public val claims: Map<String, Any?>) {
    /** When the token expires, in seconds since the epoch. */
    public val expirationTimestamp: Long get() = claims.long("exp")

    /** When the user authenticated, in seconds since the epoch. */
    public val authTimestamp: Long get() = claims.long("auth_time")

    /** When the token was issued, in seconds since the epoch. */
    public val issuedAtTimestamp: Long get() = claims.long("iat")

    /** The provider the user signed in with, e.g. `password`, or null. */
    public val signInProvider: String? get() = firebaseClaim("sign_in_provider")

    /** The second factor the user signed in with, e.g. `phone`, or null. */
    public val signInSecondFactor: String? get() = firebaseClaim("sign_in_second_factor")

    private fun firebaseClaim(name: String): String? = (claims["firebase"] as? Map<*, *>)?.get(name) as? String

    private fun Map<String, Any?>.long(name: String): Long = (this[name] as? Number)?.toLong() ?: 0L
}

/**
 * A profile update, mirroring `com.google.firebase.auth.UserProfileChangeRequest`; built with [Builder] or the
 * `userProfileChangeRequest` extension. The photo URI is Android-only.
 */
public expect class UserProfileChangeRequest {
    public val displayName: String?

    public class Builder {
        public constructor()

        public fun setDisplayName(displayName: String?): Builder
        public fun build(): UserProfileChangeRequest
    }
}
