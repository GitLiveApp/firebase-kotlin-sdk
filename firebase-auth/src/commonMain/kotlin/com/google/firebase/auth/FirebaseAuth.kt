/*
 * Copyright (c) 2026 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package com.google.firebase.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp

/**
 * The entry point of Firebase Authentication, mirroring `com.google.firebase.auth.FirebaseAuth` from the Firebase
 * Android SDK: obtained per [FirebaseApp] with the static accessors (or the `Firebase.auth` extensions), it signs users
 * in and out, creates accounts, handles email actions and reports the signed-in user and its token to listeners.
 *
 * Not mirrored: the `Activity`-bound provider sign-in flows and the SDK-internal token accessors (see
 * `api/android-sdk/exclusions.txt`).
 */
public expect class FirebaseAuth {
    /** The [FirebaseApp] this instance belongs to. */
    public val app: FirebaseApp

    /** The signed-in user, or null. */
    public val currentUser: FirebaseUser?

    /** The language of the emails and SMS the SDK sends, or null for the project default. */
    public val languageCode: String?

    /** The tenant of the Identity Platform project, or null for the parent project. */
    public val tenantId: String?

    /** The custom auth domain used for the provider sign-in flows, or null for the project default. */
    public val customAuthDomain: String?

    /** Sets [languageCode]; the SDK's getter is nullable and its setter is not, so, as in the Android SDK, this is not a property setter. */
    public fun setLanguageCode(languageCode: String)

    /** Sets [tenantId]. */
    public fun setTenantId(tenantId: String)

    /** Sets [customAuthDomain]. */
    public fun setCustomAuthDomain(customAuthDomain: String)

    /** The settings of this instance, for tests. */
    public val firebaseAuthSettings: FirebaseAuthSettings

    /** Sets [languageCode] to the device's language. */
    public fun useAppLanguage()

    /** Connects to the Authentication emulator at [host]:[port]; must be called before the instance is used. */
    public fun useEmulator(host: String, port: Int)

    /** Signs out the current user. */
    public fun signOut()

    /** Whether [link] is a sign-in link sent by [sendSignInLinkToEmail]. */
    public fun isSignInWithEmailLink(link: String): Boolean

    /** Registers [listener], called now and whenever the signed-in user changes. */
    public fun addAuthStateListener(listener: AuthStateListener)

    public fun removeAuthStateListener(listener: AuthStateListener)

    /** Registers [listener], called now and whenever the signed-in user or its ID token changes. */
    public fun addIdTokenListener(listener: IdTokenListener)

    public fun removeIdTokenListener(listener: IdTokenListener)

    /** Applies the out-of-band [code] of an email action. */
    public fun applyActionCode(code: String): Task<Nothing?>

    /** Checks the out-of-band [code] of an email action and completes with what it does. */
    public fun checkActionCode(code: String): Task<ActionCodeResult>

    /** Sets [newPassword] with the [code] of a password reset email. */
    public fun confirmPasswordReset(code: String, newPassword: String): Task<Nothing?>

    /** Creates an account for [email] with [password] and signs it in. */
    public fun createUserWithEmailAndPassword(email: String, password: String): Task<AuthResult>

    /** The sign-in methods [email] can use; only works with email enumeration protection disabled. */
    @Deprecated("Migrating off of this method is recommended as a security best-practice. Learn more in the Identity Platform documentation for [Email Enumeration Protection](https://cloud.google.com/identity-platform/docs/admin/email-enumeration-protection).")
    public fun fetchSignInMethodsForEmail(email: String): Task<SignInMethodQueryResult>

    /** Initializes the reCAPTCHA configuration, so that the first sign-in does not have to. */
    public fun initializeRecaptchaConfig(): Task<Nothing?>

    /** Revokes the Sign in with Apple [token] of the current user. */
    public fun revokeAccessToken(token: String): Task<Nothing?>

    /** Sends a password reset email to [email]. */
    public fun sendPasswordResetEmail(email: String): Task<Nothing?>

    /** Sends a password reset email to [email] whose link follows [actionCodeSettings], when given. */
    public fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings?): Task<Nothing?>

    /** Sends a sign-in link to [email] following [actionCodeSettings]. */
    public fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings): Task<Nothing?>

    /** Signs in an anonymous user, or returns the current one. */
    public fun signInAnonymously(): Task<AuthResult>

    /** Signs in with [credential] from a provider. */
    public fun signInWithCredential(credential: AuthCredential): Task<AuthResult>

    /** Signs in with a custom [token] minted by the Admin SDK. */
    public fun signInWithCustomToken(token: String): Task<AuthResult>

    /** Signs in with [email] and [password]. */
    public fun signInWithEmailAndPassword(email: String, password: String): Task<AuthResult>

    /** Signs in with the sign-in [link] sent to [email]. */
    public fun signInWithEmailLink(email: String, link: String): Task<AuthResult>

    /** Makes [user] the current user of this instance. */
    public fun updateCurrentUser(user: FirebaseUser): Task<Nothing?>

    /** Checks the [code] of a password reset email and completes with the email it was sent to. */
    public fun verifyPasswordResetCode(code: String): Task<String>

    /** Listens for changes of the signed-in user. */
    public fun interface AuthStateListener {
        public fun onAuthStateChanged(auth: FirebaseAuth)
    }

    /** Listens for changes of the signed-in user and of its ID token. */
    public fun interface IdTokenListener {
        public fun onIdTokenChanged(auth: FirebaseAuth)
    }

    public companion object {
        /** The instance of the default [FirebaseApp]. */
        public fun getInstance(): FirebaseAuth

        /** The instance of [app]. */
        public fun getInstance(app: FirebaseApp): FirebaseAuth
    }
}

/**
 * Test settings of a [FirebaseAuth] instance, mirroring `com.google.firebase.auth.FirebaseAuthSettings`.
 *
 * Not mirrored: `forceRecaptchaFlowForTesting` and `setAutoRetrievedSmsCodeForPhoneNumber`, which the iOS and JS SDKs do not have.
 */
public expect abstract class FirebaseAuthSettings() {
    /** Disables app verification for phone authentication, so that tests need no reCAPTCHA or Play Integrity. */
    public abstract fun setAppVerificationDisabledForTesting(disabled: Boolean)
}
